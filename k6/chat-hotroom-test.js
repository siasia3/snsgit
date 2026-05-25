/**
 * 시나리오 2: 핫룸 집중 부하테스트
 *
 * - 100 VU가 동일한 채팅방에 연결
 * - 메시지 1개 전송 시 100명에게 브로드캐스트 → 브로드캐스트 부하 측정
 * - 측정 포인트: 브로드캐스트 처리량, 수신 메시지 수, 에러율
 *
 * 사전 조건: k6/create-test-accounts.js 실행 완료 (loadtest_1~101 필요)
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

const connectSuccess = new Rate('ws_connect_success');
const msgsReceived   = new Counter('chat_broadcast_received');

const VUS          = 100;
const MSGS_PER_VU  = 10;
const MSG_INTERVAL = 500;  // 빠른 간격으로 브로드캐스트 부하
const TIMEOUT_MS   = 30000;

export const options = {
    stages: [
        { duration: '10s', target: VUS },  // 점진적 연결
        { duration: '30s', target: VUS },  // 피크 구간
        { duration: '10s', target: 0   },  // 종료
    ],
    thresholds: {
        ws_connect_success:  ['rate>0.95'],
        ws_session_duration: ['p(95)<20000'],
    },
};

export function setup() {
    console.log('setup: 핫룸 채팅방 생성');

    // 방장: loadtest_1, 상대: loadtest_2
    const host   = loginAndGetInfo('loadtest_1');
    const guest  = loginAndGetInfo('loadtest_2');
    if (!host || !guest) throw new Error('핫룸 setup 로그인 실패');

    const chatRoomId = getOrCreateChatRoom(host.memberId, host.cookie, guest.memberId);
    if (!chatRoomId) throw new Error('핫룸 채팅방 생성 실패');

    // 나머지 VU들도 각자 로그인 (채팅방에 입장해 메시지 보낼 주체)
    const sessions = [];
    for (let i = 1; i <= VUS; i++) {
        const user = loginAndGetInfo(`loadtest_${i}`);
        if (user) sessions.push({ cookie: user.cookie, memberId: user.memberId });
    }

    console.log(`핫룸 ID: ${chatRoomId}, 세션 수: ${sessions.length}`);
    return { chatRoomId, sessions };
}

export default function (data) {
    const { chatRoomId, sessions } = data;
    const idx                      = (__VU - 1) % sessions.length;
    const { cookie, memberId }     = sessions[idx];

    const url = makeSockJSUrl();
    const res = ws.connect(url, { headers: { Cookie: cookie } }, function (socket) {
        let msgCount = 0;

        socket.on('message', (data) => {
            if (data === 'o') {
                socket.send(stompFrame('CONNECT', {
                    'accept-version': '1.1,1.2',
                    'heart-beat':     '0,0',
                }));
                return;
            }
            if (data === 'h') return;
            if (!data.startsWith('a')) return;

            let frames;
            try { frames = JSON.parse(data.slice(1)); } catch (_) { return; }

            frames.forEach((frame) => {
                if (frame.startsWith('CONNECTED')) {
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
                            destination:    '/pub/chat/send',
                            'content-type': 'application/json',
                        }, chatPayload(chatRoomId, memberId, `hotroom blast ${msgCount + 1}`)));
                        msgCount++;
                    }, MSG_INTERVAL);
                } else if (frame.startsWith('MESSAGE')) {
                    // 100명에게 브로드캐스트된 메시지 수신
                    msgsReceived.add(1);
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