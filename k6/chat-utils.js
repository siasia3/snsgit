/**
 * WebSocket 채팅 부하테스트 공통 유틸
 *
 * SockJS + STOMP 프레임 직접 구현
 * 엔드포인트: ws://host/ws-chat/{serverId}/{sessionId}/websocket
 * 구독: /sub/chat/room/{chatRoomId}
 * 발행: /pub/chat/send
 */

import http from 'k6/http';
import { BASE_URL, PASSWORD } from './accounts.js';

export const WS_BASE = BASE_URL.replace(/^http/, 'ws');

/**
 * 로그인 후 쿠키 + memberId 반환
 */
export function loginAndGetInfo(userId) {
    const loginRes = http.post(
        `${BASE_URL}/api/login`,
        JSON.stringify({ userId, password: PASSWORD }),
        { headers: { 'Content-Type': 'application/json' }, redirects: 0 }
    );
    if (loginRes.status !== 200 || !loginRes.cookies['Authorization']) return null;

    const cookie = `Authorization=${loginRes.cookies['Authorization'][0].value}`;

    const infoRes = http.get(`${BASE_URL}/api/member/info`, {
        headers: { Cookie: cookie },
    });
    if (infoRes.status !== 200) return null;

    const info = JSON.parse(infoRes.body);
    return { cookie, memberId: info.memberId };
}

/**
 * 채팅방 getOrCreate
 * senderId/receiverId는 memberId(Long)
 */
export function getOrCreateChatRoom(senderId, cookie, receiverId) {
    const res = http.get(
        `${BASE_URL}/api/chatroom/member?senderId=${senderId}&receiverId=${receiverId}`,
        { headers: { Cookie: cookie } }
    );
    if (res.status !== 200) return null;
    return JSON.parse(res.body).chatroomId;
}

/**
 * SockJS WebSocket 전송 URL 생성
 * 형식: {base}/ws-chat/{serverId}/{sessionId}/websocket
 */
export function makeSockJSUrl() {
    const serverId = String(Math.floor(Math.random() * 900) + 100);
    const chars = 'abcdefghijklmnopqrstuvwxyz0123456789';
    let sessionId = '';
    for (let i = 0; i < 12; i++) {
        sessionId += chars[Math.floor(Math.random() * chars.length)];
    }
    return `${WS_BASE}/ws-chat/${serverId}/${sessionId}/websocket`;
}

/**
 * SockJS 래핑 STOMP 프레임 생성
 * SockJS 프로토콜: ["COMMAND\nheader:value\n\nbody\u0000"]
 */
export function stompFrame(command, headers = {}, body = '') {
    let frame = command + '\n';
    Object.entries(headers).forEach(([k, v]) => {
        frame += `${k}:${v}\n`;
    });
    frame += '\n' + body + '\u0000';
    return JSON.stringify([frame]);
}

/**
 * /pub/chat/send 용 메시지 JSON 페이로드
 */
export function chatPayload(chatRoomId, senderId, text) {
    return JSON.stringify({
        type: 'TALK',
        chatRoomId: chatRoomId,
        senderId: senderId,
        message: text,
    });
}