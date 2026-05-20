import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Rate } from 'k6/metrics';

// 커스텀 메트릭
const loginDuration = new Trend('login_duration');
const postsDuration = new Trend('posts_duration');
const errorRate = new Rate('error_rate');

export const options = {
    scenarios: {
        // 점진적 부하 증가
        ramp_up: {
            executor: 'ramping-vus',
            startVUs: 1,
            stages: [
                { duration: '10s', target: 10 },  // 10초 동안 10명으로 증가
                { duration: '30s', target: 10 },  // 30초 유지
                { duration: '10s', target: 30 },  // 10초 동안 30명으로 증가
                { duration: '20s', target: 30 },  // 20초 유지
                { duration: '10s', target: 0 },   // 10초 동안 0으로 감소
            ],
        },
    },
    thresholds: {
        http_req_duration: ['p(95)<500'],  // 95%의 요청이 500ms 이내
        error_rate: ['rate<0.1'],           // 에러율 10% 미만
    },
};

// 테스트 계정 설정 (실제 존재하는 계정으로 변경)
const BASE_URL = 'http://localhost';
const TEST_ACCOUNT = {
    userId: 'demo',      // 실제 테스트 계정으로 변경
    password: 'demo1234', // 실제 비밀번호로 변경
};

export default function () {
    // 1. 로그인 - JWT 쿠키 획득
    const loginStart = Date.now();
    const loginRes = http.post(
        `${BASE_URL}/api/login`,
        JSON.stringify({
            userId: TEST_ACCOUNT.userId,
            password: TEST_ACCOUNT.password,
        }),
        {
            headers: { 'Content-Type': 'application/json' },
            redirects: 0,
        }
    );
    loginDuration.add(Date.now() - loginStart);

    const loginOk = check(loginRes, {
        '로그인 성공 (200)': (r) => r.status === 200,
        '쿠키 존재': (r) => r.cookies['Authorization'] !== undefined,
    });

    if (!loginOk) {
        errorRate.add(1);
        console.log(`로그인 실패: status=${loginRes.status}, body=${loginRes.body}`);
        return;
    }
    errorRate.add(0);

    // 쿠키 추출
    const authCookie = loginRes.cookies['Authorization'][0].value;
    const cookieHeader = `Authorization=${authCookie}`;
    const headers = {
        'Content-Type': 'application/json',
        Cookie: cookieHeader,
    };

    sleep(0.5);

    // 2. 게시글 목록 조회 (커서 기반)
    const postsStart = Date.now();
    const postsRes = http.get(`${BASE_URL}/api/posts`, { headers });
    postsDuration.add(Date.now() - postsStart);

    const postsOk = check(postsRes, {
        '게시글 목록 조회 성공 (200)': (r) => r.status === 200,
    });
    errorRate.add(postsOk ? 0 : 1);

    sleep(0.5);

    // 3. 게시글 상세 조회 (postId=1 고정 - 실제 존재하는 ID로 변경)
    const postDetailRes = http.get(`${BASE_URL}/api/post/1`, { headers });
    check(postDetailRes, {
        '게시글 상세 조회 성공 (200)': (r) => r.status === 200,
    });
    errorRate.add(postDetailRes.status === 200 ? 0 : 1);

    sleep(1);
}