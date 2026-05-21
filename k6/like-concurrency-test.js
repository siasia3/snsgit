import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';

/**
 * 좋아요 동시성 테스트
 *
 * 목적: 여러 유저가 동시에 같은 게시글에 좋아요를 누를 때
 *       중복 좋아요가 발생하지 않는지, 에러 없이 처리되는지 확인
 *
 * 실행: k6 run k6/like-concurrency-test.js
 */

const likeDuration = new Trend('like_duration');
const unlikeDuration = new Trend('unlike_duration');
const errorRate = new Rate('error_rate');
const duplicateLikeCount = new Counter('duplicate_like_count');

export const options = {
    scenarios: {
        // 동시에 여러 VU가 같은 postId에 좋아요 요청
        concurrent_likes: {
            executor: 'shared-iterations',
            vus: 20,
            iterations: 100,
            maxDuration: '1m',
        },
    },
    thresholds: {
        http_req_duration: ['p(95)<1000'],
        error_rate: ['rate<0.05'],  // 중복/서버 에러 5% 미만
    },
};

const BASE_URL = 'http://localhost';

// 테스트 계정 목록 (여러 계정으로 동시 요청)
const TEST_ACCOUNTS = [
    { userId: 'demo1', password: '1234' },
    { userId: 'demo2', password: '1234' },
    { userId: 'demo3', password: '1234' },
    { userId: 'demo4', password: '1234' },
    { userId: 'demo5', password: '1234' },
];

// 동시 좋아요를 테스트할 게시글 ID (실제 존재하는 ID로 변경)
const TARGET_POST_IDS = [100001, 100000, 99999];

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

export default function () {
    // VU index를 기반으로 계정 순환
    const account = TEST_ACCOUNTS[__VU % TEST_ACCOUNTS.length];
    const cookieHeader = login(account);

    if (!cookieHeader) {
        errorRate.add(1);
        return;
    }

    const headers = {
        'Content-Type': 'application/json',
        Cookie: cookieHeader,
    };

    // 랜덤 게시글에 좋아요
    const postId = TARGET_POST_IDS[Math.floor(Math.random() * TARGET_POST_IDS.length)];

    // 1. 좋아요 추가
    const likeStart = Date.now();
    const likeRes = http.post(
        `${BASE_URL}/api/like`,
        JSON.stringify({ postId }),
        { headers }
    );
    likeDuration.add(Date.now() - likeStart);

    const likeOk = check(likeRes, {
        '좋아요 성공 (200)': (r) => r.status === 200,
        '좋아요 ID 반환': (r) => r.json() > 0,
    });

    if (likeRes.status === 409) {
        // 중복 좋아요 발생 시 카운트 (중복 방지 로직이 있으면 409 혹은 별도 에러)
        duplicateLikeCount.add(1);
        errorRate.add(1);
        console.log(`[중복 좋아요] VU=${__VU}, postId=${postId}, status=${likeRes.status}`);
        return;
    }

    if (!likeOk) {
        errorRate.add(1);
        console.log(`[좋아요 실패] VU=${__VU}, postId=${postId}, status=${likeRes.status}, body=${likeRes.body}`);
        return;
    }
    errorRate.add(0);

    const likeId = likeRes.json();

    sleep(0.5);

    // 2. 좋아요 취소 (cleanup)
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