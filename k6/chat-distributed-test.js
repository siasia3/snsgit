/**
 * 시나리오 1: 분산 채팅 부하테스트
 *
 * - 100 VU가 각자 다른 1:1 채팅방에 연결
 * - 실제 서비스 트래픽 패턴에 가장 가까운 시나리오
 * - 측정 포인트: 연결 수, 연결 안정성, 메모리 누수
 *
 * 사전 조건: k6/create-test-accounts.js 실행 완료 (loadtest_1~200 필요)
 */

import ws from 'k6/ws';
import { check } from 'k6';
import { Rate } from 'k6/metrics';
import {
    loginAndGetInfo,
    getOrCreateChatRoom,
    makeSockJSUrl,
    stompFrame,
    chatPayload,
} from './chat-utils.js';

const connectSuccess = new Rate('ws_connect_success');

const PAIRS        = 100;  // 채팅방 수 (VU 수와 동일)
const MSGS_PER_VU  = 10;   // VU당 전송 메시지 수
const MSG_INTERVAL = 1000; // 메시지 간격 (ms)
const TIMEOUT_MS   = 30000;

export const options = {
    vus: PAIRS,
    duration: '60s',
    thresholds: {
        ws_connect_success:   ['rate>0.95'],
        ws_session_duration:  ['p(95)<15000'],
    },
};

export function setup() {
    console.log('setup: 계정 로그인 + 채팅방 생성 시작');
    const sessions = [];

    for (let i = 1; i <= PAIRS; i++) {
        const sender   = loginAndGetInfo(`loadtest_${i}`);
        const receiver = loginAndGetInfo(`loadtest_${i + PAIRS}`);
        if (!sender || !receiver) {
            console.warn(`pair ${i} 로그인 실패, 스킵`);
            continue;
        }

        const chatRoomId = getOrCreateChatRoom(sender.memberId, sender.cookie, receiver.memberId);
        if (!chatRoomId) {
            console.warn(`pair ${i} 채팅방 생성 실패, 스킵`);
            continue;
        }

        sessions.push({
            cookie:     sender.cookie,
            memberId:   sender.memberId,
            chatRoomId: chatRoomId,
        });
    }

    console.log(`setup 완료: ${sessions.length}개 세션 준비`);
    return sessions;
}

export default function (sessions) {
    const idx                          = (__VU - 1) % sessions.length;
    const { cookie, memberId, chatRoomId } = sessions[idx];

    const url = makeSockJSUrl();
    const res = ws.connect(url, { headers: { Cookie: cookie } }, function (socket) {
        let msgCount = 0;

        socket.on('open', () => {
            // SockJS는 open 이후 'o' 프레임을 먼저 보냄 → message 핸들러에서 처리
        });

        socket.on('message', (data) => {
            // SockJS open 프레임
            if (data === 'o') {
                socket.send(stompFrame('CONNECT', {
                    'accept-version': '1.1,1.2',
                    'heart-beat':     '0,0',
                }));
                return;
            }
            if (data === 'h') return; // heartbeat

            if (!data.startsWith('a')) return;

            let frames;
            try { frames = JSON.parse(data.slice(1)); } catch (_) { return; }

            frames.forEach((frame) => {
                if (frame.startsWith('CONNECTED')) {
                    // 구독 후 메시지 전송 시작
                    socket.send(stompFrame('SUBSCRIBE', {
                        id:          'sub-0',
                        destination: `/sub/chat/room/${chatRoomId}`,
                    }));

                    socket.setInterval(() => {
                        if (msgCount >= MSGS_PER_VU) {
                            socket.send(stompFrame('DISCONNECT', {}));
                            socket.close();
                            return;
                        }
                        socket.send(stompFrame('SEND', {
                            destination:     '/pub/chat/send',
                            'content-type':  'application/json',
                        }, chatPayload(chatRoomId, memberId, `distributed msg ${msgCount + 1}`)));
                        msgCount++;
                    }, MSG_INTERVAL);
                }
            });
        });

        socket.on('error', (e) => {
            console.error(`VU${__VU} WS error: ${e.error()}`);
        });

        socket.setTimeout(() => socket.close(), TIMEOUT_MS);
    });

    check(res, { 'ws status 101': (r) => r && r.status === 101 });
    connectSuccess.add(res && res.status === 101);
}