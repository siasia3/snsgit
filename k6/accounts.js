/**
 * 공통 계정 풀
 *
 * create-test-accounts.js 실행 후 사용 가능
 * 각 테스트 스크립트에서 import해서 사용
 */

import http from 'k6/http';

export const BASE_URL       = 'http://localhost';
export const PASSWORD       = '1234';
export const TOTAL_ACCOUNTS = 1000;

/**
 * 테스트에 사용할 계정 목록 생성
 * @param {number} count - 사용할 계정 수 (기본: TOTAL_ACCOUNTS)
 */
export function getAccounts(count = TOTAL_ACCOUNTS) {
    const accounts = [];
    for (let i = 1; i <= count; i++) {
        accounts.push({ userId: `loadtest_${i}`, password: PASSWORD });
    }
    return accounts;
}

/**
 * 계정 로그인 후 쿠키 헤더 반환
 * 실패 시 null 반환
 */
export function login(account) {
    const res = http.post(
        `${BASE_URL}/api/login`,
        JSON.stringify({ userId: account.userId, password: account.password }),
        { headers: { 'Content-Type': 'application/json' }, redirects: 0 }
    );
    if (res.status !== 200 || !res.cookies['Authorization']) return null;
    return `Authorization=${res.cookies['Authorization'][0].value}`;
}

/**
 * setup()에서 사용: n개 계정을 로그인해서 세션 배열 반환
 * n은 최대 VU 수와 맞추면 됨 (예: vus: 50 → setupSessions(50))
 */
export function setupSessions(count) {
    const accounts = getAccounts(count);
    return accounts.map(account => ({
        userId:       account.userId,
        cookieHeader: login(account),
    }));
}