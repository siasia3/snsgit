/**
 * 시나리오 3: 혼합 부하테스트
 *
 * - 50 VU → 핫룸 (같은 채팅방, 브로드캐스트 부하)
 * - 50 VU → 분산 (각자 다른 채팅방, 실제 트래픽)
 * - 실제 서비스에서 두 패턴이 혼재하는 상황 시뮬레이션
 *
 * 사전 조건: k6/create-test-accounts.js 실행 완료 (loadtest_1~152 필요)
 */

import ws from 'k6/ws';
import { check } from 'k6';
import { Counter, Rate } from 'k6/metrics';
import {
    loginAndGetInfo,
    getOrCreateChatRoom,
    makeSockJSUrl,
    stompFrame,
    chatPayload,
} from './chat-utils.js';

const connectSuccess     = new Rate('ws_connect_success');
const hotBroadcastRcv    = new Counter('chat_hot_broadcast_received');
const distributedMsgsSnt = new Counter('chat_distributed_msgs_sent');

const HOT_VUS  = 50;
const DIST_VUS = 50;
const MSGS_PER_VU  = 10;
const TIMEOUT_MS   = 35000;

export const options = {
    scenarios: {
        hotroom: {
            executor:  'constant-vus',
            vus:       HOT_VUS,
            duration:  '40s',
            exec:      'hotRoomVU',
            startTime: '5s',  // 분산 VU들 먼저 연결되게 지연
        },
        distributed: {
            executor: 'ramping-vus',
            stages: [
                { duration: '5s',  target: DIST_VUS },
                { duration: '30s', target: DIST_VUS },
                { duration: '5s',  target: 0        },
            ],
            exec: 'distributedVU',
        },
    },
    thresholds: {
        ws_connect_success:  ['rate>0.95'],
        ws_session_duration: ['p(95)<20000'],
    },
};

export function setup() {
    console.log('setup 시작: 핫룸 1개 + 분산 채팅방 50개 준비');

    // ── 핫룸 ──────────────────────────────────────────
    const hotHost  = loginAndGetInfo('loadtest_1');
    const hotGuest = loginAndGetInfo('loadtest_2');
    if (!hotHost || !hotGuest) throw new Error('핫룸 setup 로그인 실패');

    const hotChatRoomId = getOrCreateChatRoom(hotHost.memberId, hotHost.cookie, hotGuest.memberId);
    if (!hotChatRoomId) throw new Error('핫룸 채팅방 생성 실패');

    const hotSessions = [];
    for (let i = 1; i <= HOT_VUS; i++) {
        const user = loginAndGetInfo(`loadtest_${i}`);
        if (user) hotSessions.push({ cookie: user.cookie, memberId: user.memberId });
    }

    // ── 분산 채팅방 ────────────────────────────────────
    // sender: loadtest_101~150, receiver: loadtest_151~200
    const distSessions = [];
    for (let i = 1; i <= DIST_VUS; i++) {
        const sender   = loginAndGetInfo(`loadtest_${100 + i}`);
        const receiver = loginAndGetInfo(`loadtest_${150 + i}`);
        if (!sender || !receiver) {
            console.warn(`분산 pair ${i} 로그인 실패, 스킵`);
            continue;
        }
        const chatRoomId = getOrCreateChatRoom(sender.memberId, sender.cookie, receiver.memberId);
        if (!chatRoomId) {
            console.warn(`분산 pair ${i} 채팅방 생성 실패, 스킵`);
            continue;
        }
        distSessions.push({ cookie: sender.cookie, memberId: sender.memberId, chatRoomId });
    }

    console.log(`setup 완료 — 핫룸: ${hotChatRoomId}, 분산 세션: ${distSessions.length}개`);
    return { hotChatRoomId, hotSessions, distSessions };
}

// ── 핫룸 VU ─────────────────────────────────────────────────────────────────
export function hotRoomVU(data) {
    const { hotChatRoomId, hotSessions } = data;
    const idx                            = (__VU - 1) % hotSessions.length;
    const { cookie, memberId }           = hotSessions[idx];

    const url = makeSockJSUrl();
    const res = ws.connect(url, { headers: { Cookie: cookie } }, function (socket) {
        let msgCount = 0;

        socket.on('message', (d) => {
            if (d === 'o') {
                socket.send(stompFrame('CONNECT', { 'accept-version': '1.1,1.2', 'heart-beat': '0,0' }));
                return;
            }
            if (d === 'h') return;
            if (!d.startsWith('a')) return;

            let frames;
            try { frames = JSON.parse(d.slice(1)); } catch (_) { return; }

            frames.forEach((frame) => {
                if (frame.startsWith('CONNECTED')) {
                    socket.send(stompFrame('SUBSCRIBE', {
                        id: 'sub-0', destination: `/sub/chat/room/${hotChatRoomId}`,
                    }));
                    socket.setInterval(() => {
                        if (msgCount >= MSGS_PER_VU) {
                            socket.send(stompFrame('DISCONNECT', {}));
                            socket.close();
                            return;
                        }
                        socket.send(stompFrame('SEND', {
                            destination: '/pub/chat/send', 'content-type': 'application/json',
                        }, chatPayload(hotChatRoomId, memberId, `hot ${msgCount + 1}`)));
                        msgCount++;
                    }, 600);
                } else if (frame.startsWith('MESSAGE')) {
                    hotBroadcastRcv.add(1);
                }
            });
        });

        socket.on('error', (e) => console.error(`[hot] VU${__VU} error: ${e.error()}`));
        socket.setTimeout(() => socket.close(), TIMEOUT_MS);
    });

    check(res, { 'ws status 101': (r) => r && r.status === 101 });
    connectSuccess.add(res && res.status === 101);
}

// ── 분산 VU ──────────────────────────────────────────────────────────────────
export function distributedVU(data) {
    const { distSessions }                 = data;
    const idx                              = (__VU - 1) % distSessions.length;
    const { cookie, memberId, chatRoomId } = distSessions[idx];

    const url = makeSockJSUrl();
    const res = ws.connect(url, { headers: { Cookie: cookie } }, function (socket) {
        let msgCount = 0;

        socket.on('message', (d) => {
            if (d === 'o') {
                socket.send(stompFrame('CONNECT', { 'accept-version': '1.1,1.2', 'heart-beat': '0,0' }));
                return;
            }
            if (d === 'h') return;
            if (!d.startsWith('a')) return;

            let frames;
            try { frames = JSON.parse(d.slice(1)); } catch (_) { return; }

            frames.forEach((frame) => {
                if (frame.startsWith('CONNECTED')) {
                    socket.send(stompFrame('SUBSCRIBE', {
                        id: 'sub-0', destination: `/sub/chat/room/${chatRoomId}`,
                    }));
                    socket.setInterval(() => {
                        if (msgCount >= MSGS_PER_VU) {
                            socket.send(stompFrame('DISCONNECT', {}));
                            socket.close();
                            return;
                        }
                        socket.send(stompFrame('SEND', {
                            destination: '/pub/chat/send', 'content-type': 'application/json',
                        }, chatPayload(chatRoomId, memberId, `dist ${msgCount + 1}`)));
                        distributedMsgsSnt.add(1);
                        msgCount++;
                    }, 1000);
                }
            });
        });

        socket.on('error', (e) => console.error(`[dist] VU${__VU} error: ${e.error()}`));
        socket.setTimeout(() => socket.close(), TIMEOUT_MS);
    });

    check(res, { 'ws status 101': (r) => r && r.status === 101 });
    connectSuccess.add(res && res.status === 101);
}