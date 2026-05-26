import http from 'k6/http';
import { check } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';
import { BASE_URL, getAccounts } from './accounts.js';

/**
 * 로그인 폭격 (인증 부하 테스트)
 *
 * 목적: 동시 다수 유저가 로그인할 때
 *       Spring Security 필터 체인 + JWT 발급 처리량(TPS) 및 응답시간 검증
 *
 * 시나리오: constant-arrival-rate로 초당 고정 요청 수를 주입하여 최대 TPS 측정
 *
 * 사전 준비: k6 run k6/create-test-accounts.js
 * 실행:      k6 run k6/login-load-test.js
 */

const loginDuration = new Trend('login_duration');
const errorRate     = new Rate('error_rate');
const loginSuccess  = new Counter('login_success');
const jwtIssued     = new Counter('jwt_issued');

// constant-arrival-rate: 초당 목표 요청 수
const TARGET_RPS      = 50;   // 초당 50 로그인 요청
const PREALLOC_VUS    = 50;   // 사전 할당 VU
const MAX_VUS         = 100;  // 최대 VU
const TEST_ACCOUNTS   = getAccounts(1000);

export const options = {
    scenarios: {
        login_constant_rate: {
            executor: 'constant-arrival-rate',
            rate: TARGET_RPS,
            timeUnit: '1s',
            duration: '60s',
            preAllocatedVUs: PREALLOC_VUS,
            maxVUs: MAX_VUS,
        },
    },
    thresholds: {
        login_duration:    ['p(95)<500', 'p(99)<1000'],
        error_rate:        ['rate<0.01'],  // 로그인은 에러가 거의 없어야 함
        http_req_duration: ['p(95)<500'],
    },
};

export default function () {
    // 각 iteration마다 다른 계정 순환 사용
    const account = TEST_ACCOUNTS[__ITER % TEST_ACCOUNTS.length];

    const start = Date.now();
    const res = http.post(
        `${BASE_URL}/api/login`,
        JSON.stringify({ userId: account.userId, password: account.password }),
        {
            headers: { 'Content-Type': 'application/json' },
            redirects: 0,
        }
    );
    loginDuration.add(Date.now() - start);

    const ok = check(res, {
        '로그인 성공 (200)':   (r) => r.status === 200,
        'JWT 쿠키 발급':       (r) => r.cookies['Authorization'] !== undefined,
    });

    if (ok) {
        loginSuccess.add(1);
        jwtIssued.add(1);
        errorRate.add(0);
    } else {
        errorRate.add(1);
        console.log(`[로그인 실패] userId=${account.userId}, status=${res.status}, body=${res.body}`);
    }

    // 로그인 폭격 시나리오 - sleep 없음 (constant-arrival-rate가 속도 제어)
}