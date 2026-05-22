import http from 'k6/http';
import { Counter } from 'k6/metrics';
import exec from 'k6/execution';

/**
 * 부하 테스트용 계정 1000개 생성
 *
 * 실행: k6 run k6/create-test-accounts.js
 * 생성 계정: loadtest_1 ~ loadtest_1000 (비밀번호: 1234)
 *
 * 주의: 부하 테스트 전 딱 한 번만 실행하면 됩니다.
 *       이미 생성된 계정은 409로 스킵됩니다.
 */

const createdCount  = new Counter('accounts_created');
const skippedCount  = new Counter('accounts_skipped');  // 이미 존재
const failedCount   = new Counter('accounts_failed');

export const options = {
    scenarios: {
        create_accounts: {
            executor: 'shared-iterations',
            vus: 10,        // 동시 10개씩 생성
            iterations: 1000,
            maxDuration: '3m',
        },
    },
    thresholds: {
        accounts_failed: ['count<10'],  // 10개 이상 실패 시 이상 신호
    },
};

const BASE_URL = 'http://localhost';
export const PASSWORD = '1234';

export default function () {
    const n        = exec.scenario.iterationInTest + 1;  // 1 ~ 1000
    const userId   = `loadtest_${n}`;
    const nickname = `test${n}`;

    const res = http.post(
        `${BASE_URL}/api/member/signup`,
        {
            userId,
            password:  PASSWORD,
            nickname,
            name:      `테스트${n}`,
            email:     `loadtest${n}@test.com`,
            birthdate: '2000-01-01',
            gender:    'M',
        },
        { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } }
    );

    if (res.status === 201) {
        createdCount.add(1);
    } else if (res.status === 409) {
        // 이미 존재하는 계정 - 정상 스킵
        skippedCount.add(1);
    } else {
        failedCount.add(1);
        console.log(`[계정 생성 실패] userId=${userId}, status=${res.status}, body=${res.body}`);
    }
}