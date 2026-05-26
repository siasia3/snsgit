import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';
import encoding from 'k6/encoding';
import { BASE_URL, setupSessions } from './accounts.js';

/**
 * 게시글 쓰기 부하 테스트
 *
 * 목적: 다수 유저가 동시에 게시글을 작성할 때
 *       DB insert + OCI Object Storage 업로드 처리량과 안정성 검증
 *
 * 주의: 실제 OCI에 이미지를 업로드하므로 스토리지 비용 발생 가능
 *       테스트 후 생성된 게시글은 수동으로 정리 필요 (API가 postId를 반환하지 않음)
 *
 * 사전 준비: k6 run k6/create-test-accounts.js
 * 실행:      k6 run k6/post-write-load-test.js
 */

const writeDuration = new Trend('post_write_duration');
const errorRate     = new Rate('error_rate');
const writeSuccess  = new Counter('post_write_success');

// 1x1 투명 PNG (최소 유효 이미지, 67 bytes)
const DUMMY_IMAGE_B64 = 'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==';

const MAX_VUS = 30;

export const options = {
    scenarios: {
        post_write_ramp: {
            executor: 'ramping-vus',
            startVUs: 1,
            stages: [
                { duration: '10s', target: 10  },  // 워밍업
                { duration: '20s', target: 10  },  // 낮은 부하 유지
                { duration: '10s', target: MAX_VUS },  // 피크
                { duration: '30s', target: MAX_VUS },  // 피크 유지
                { duration: '10s', target: 0   },      // 감소
            ],
        },
    },
    thresholds: {
        post_write_duration: ['p(95)<3000'],  // OCI 업로드 포함이라 여유있게 설정
        error_rate:          ['rate<0.05'],
    },
};

export function setup() {
    return setupSessions(MAX_VUS);
}

export default function (sessions) {
    const session = sessions[__VU % sessions.length];

    if (!session.cookieHeader) {
        errorRate.add(1);
        console.log(`[로그인 세션 없음] VU=${__VU}, userId=${session.userId}`);
        return;
    }

    // 1x1 PNG 이미지 바이너리 생성
    const imageBytes = encoding.b64decode(DUMMY_IMAGE_B64, 'std', 'b');

    // multipart/form-data 구성
    // postContent: @RequestPart (application/json)
    // files: @RequestPart (image/png)
    const formData = {
        postContent: http.file(
            JSON.stringify({ postContent: `부하테스트 게시글 - VU${__VU} iter${__ITER}` }),
            'blob',
            'application/json'
        ),
        files: http.file(imageBytes, 'test.png', 'image/png'),
    };

    const start = Date.now();
    const res = http.post(
        `${BASE_URL}/api/post`,
        formData,
        { headers: { Cookie: session.cookieHeader } }
    );
    writeDuration.add(Date.now() - start);

    const ok = check(res, {
        '게시글 작성 성공 (200)': (r) => r.status === 200,
        '성공 메시지 포함':       (r) => {
            try { return r.json('message') !== undefined; } catch { return false; }
        },
    });

    if (ok) {
        writeSuccess.add(1);
        errorRate.add(0);
    } else {
        errorRate.add(1);
        console.log(`[게시글 작성 실패] VU=${__VU}, userId=${session.userId}, status=${res.status}, body=${res.body}`);
    }

    // 유저가 업로드 후 잠깐 기다리는 행동 시뮬레이션
    sleep(1);
}