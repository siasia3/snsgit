import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';

/**
 * 좋아요 동시성 테스트
 *
 * 목적: 여러 VU가 동일한 (user, post) 조합으로 동시에 좋아요를 눌러
 *       DB unique constraint 충돌 상황에서 중복 없이 처리되는지 확인
 *
 * 실행: k6 run k6/like-concurrency-test.js
 */

const likeDuration = new Trend('like_duration');
const unlikeDuration = new Trend('unlike_duration');
const errorRate = new Rate('error_rate');
const duplicateLikeCount = new Counter('duplicate_like_count');

export const options = {
    scenarios: {
        concurrent_likes: {
            executor: 'shared-iterations',
            vus: 20,
            iterations: 100,
            maxDuration: '1m',
        },
    },
    thresholds: {
        http_req_duration: ['p(95)<1000'],
        error_rate: ['rate<0.05'],
    },
};

const BASE_URL = 'http://localhost';

const TEST_ACCOUNTS = [
    { userId: 'demo1', password: '1234' },
    { userId: 'demo2', password: '1234' },
    { userId: 'demo3', password: '1234' },
    { userId: 'demo4', password: '1234' },
    { userId: 'demo5', password: '1234' },
];

// 모든 VU가 같은 게시글을 공격해야 (user, post) 충돌이 실제로 발생함
const TARGET_POST_ID = 100001;

function login(account) {
    const res = http.post(
        `${BASE_URL}/api/login`,
        JSON.stringify({ userId: account.userId, password: account.password }),
        { headers: { 'Content-Type': 'application/json' }, redirects: 0 }
    );

    if (res.status !== 200 || !res.cookies['Authorization']) {
        return null;
    }
    return `Authorization=${res.cookies['Authorization'][0].value}`;
}

// setup()에서 미리 로그인 → 매 iteration마다 로그인 비용 제거
export function setup() {
    return TEST_ACCOUNTS.map(account => ({
        userId: account.userId,
        cookieHeader: login(account),
    }));
}

export default function (sessions) {
    const session = sessions[__VU % sessions.length];

    if (!session.cookieHeader) {
        errorRate.add(1);
        console.log(`[로그인 실패] VU=${__VU}, userId=${session.userId}`);
        return;
    }

    const headers = {
        'Content-Type': 'application/json',
        Cookie: session.cookieHeader,
    };

    // 1. 좋아요
    const likeStart = Date.now();
    const likeRes = http.post(
        `${BASE_URL}/api/like`,
        JSON.stringify({ postId: TARGET_POST_ID }),
        { headers }
    );
    likeDuration.add(Date.now() - likeStart);

    if (likeRes.status === 409) {
        // 중복 방지 로직이 정상 동작한 것 → 에러 아님
        duplicateLikeCount.add(1);
        errorRate.add(0);
        console.log(`[중복 좋아요 차단] VU=${__VU}, userId=${session.userId}, status=409`);
        return;
    }

    const likeOk = check(likeRes, {
        '좋아요 성공 (200)': (r) => r.status === 200,
        '좋아요 ID 반환': (r) => r.json() > 0,
    });

    if (!likeOk) {
        errorRate.add(1);
        console.log(`[좋아요 실패] VU=${__VU}, userId=${session.userId}, status=${likeRes.status}, body=${likeRes.body}`);
        return;
    }
    errorRate.add(0);

    const likeId = likeRes.json();
    sleep(0.5);

    // 2. 좋아요 취소 (다음 iteration을 위한 cleanup)
    const unlikeStart = Date.now();
    const unlikeRes = http.del(
        `${BASE_URL}/api/like`,
        JSON.stringify({ likeId }),
        { headers }
    );
    unlikeDuration.add(Date.now() - unlikeStart);

    check(unlikeRes, {
        '좋아요 취소 성공 (200)': (r) => r.status === 200,
    });

    sleep(0.3);
}