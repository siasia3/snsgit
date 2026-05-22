import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';
import { BASE_URL, setupSessions } from './accounts.js';

/**
 * 피드 조회 부하 테스트
 *
 * 목적: 다수 유저가 동시에 피드를 스크롤할 때
 *       커서 기반 페이지네이션의 응답시간과 안정성 검증
 *
 * 사전 준비: k6 run k6/create-test-accounts.js
 * 실행:      k6 run k6/feed-load-test.js
 */

const feedFirstPageDuration = new Trend('feed_first_page_duration');
const feedNextPageDuration  = new Trend('feed_next_page_duration');
const errorRate             = new Rate('error_rate');
const emptyFeedCount        = new Counter('empty_feed_count');

const MAX_VUS   = 100;
const PAGE_SIZE = 10;

export const options = {
    scenarios: {
        feed_ramp_up: {
            executor: 'ramping-vus',
            startVUs: 1,
            stages: [
                { duration: '10s', target: 10  },  // 워밍업
                { duration: '20s', target: 10  },  // 낮은 부하 유지
                { duration: '10s', target: 50  },  // 중간 부하
                { duration: '20s', target: 50  },  // 중간 부하 유지
                { duration: '10s', target: MAX_VUS },  // 피크
                { duration: '30s', target: MAX_VUS },  // 피크 유지
                { duration: '10s', target: 0   },      // 감소
            ],
        },
    },
    thresholds: {
        feed_first_page_duration: ['p(95)<500'],
        feed_next_page_duration:  ['p(95)<500'],
        error_rate:               ['rate<0.05'],
    },
};

// MAX_VUS 수만큼만 로그인 (1000개 전부 로그인할 필요 없음)
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

    const headers = {
        'Content-Type': 'application/json',
        Cookie: session.cookieHeader,
    };

    // ── 1. 첫 페이지 (cursor 없음) ──────────────────────────────────────
    const firstStart = Date.now();
    const firstRes = http.get(
        `${BASE_URL}/api/posts?size=${PAGE_SIZE}`,
        { headers }
    );
    feedFirstPageDuration.add(Date.now() - firstStart);

    const firstOk = check(firstRes, {
        '피드 첫 페이지 (200)': (r) => r.status === 200,
        '게시글 목록 존재':     (r) => {
            try { return Array.isArray(r.json('content')); } catch { return false; }
        },
    });

    if (!firstOk) {
        errorRate.add(1);
        console.log(`[피드 첫 페이지 실패] VU=${__VU}, userId=${session.userId}, status=${firstRes.status}, body=${firstRes.body}`);
        return;
    }
    errorRate.add(0);

    const firstBody = firstRes.json();
    const posts     = firstBody.content;

    if (!posts || posts.length === 0) {
        emptyFeedCount.add(1);
        console.log(`[빈 피드] VU=${__VU}, userId=${session.userId}`);
        return;
    }

    // 유저가 피드를 읽는 시간 시뮬레이션
    sleep(1);

    // ── 2. 다음 페이지 (마지막 게시글 기준 커서) ────────────────────────
    if (!firstBody.hasNext) return;

    const lastPost       = posts[posts.length - 1];
    const cursorPostId   = lastPost.postId;
    const cursorCreatedAt = lastPost.createdAt; // Jackson ISO 직렬화 기준 (예: "2024-01-15T10:30:00")

    const nextStart = Date.now();
    const nextRes = http.get(
        `${BASE_URL}/api/posts?cursorPostId=${cursorPostId}&cursorCreatedAt=${encodeURIComponent(cursorCreatedAt)}&size=${PAGE_SIZE}`,
        { headers }
    );
    feedNextPageDuration.add(Date.now() - nextStart);

    const nextOk = check(nextRes, {
        '피드 다음 페이지 (200)': (r) => r.status === 200,
        '다음 페이지 게시글 존재': (r) => {
            try { return Array.isArray(r.json('content')); } catch { return false; }
        },
        '커서 중복 없음': (r) => {
            // 다음 페이지의 첫 게시글 ID가 이전 마지막 ID보다 작아야 함
            try {
                const content = r.json('content');
                return content.length === 0 || content[0].postId < cursorPostId;
            } catch { return false; }
        },
    });

    errorRate.add(nextOk ? 0 : 1);
    if (!nextOk) {
        console.log(`[피드 다음 페이지 실패] VU=${__VU}, userId=${session.userId}, status=${nextRes.status}`);
    }

    sleep(0.5);
}