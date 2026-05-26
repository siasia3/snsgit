import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';
import { BASE_URL, setupSessions } from './accounts.js';

/**
 * 댓글 쓰기 부하 테스트
 *
 * 목적: 다수 유저가 동시에 댓글을 작성할 때
 *       DB insert 처리량과 안정성 검증
 *       작성 후 삭제(cleanup)까지 측정
 *
 * 사전 준비: k6 run k6/create-test-accounts.js
 * 실행:      k6 run k6/comment-load-test.js
 */

const commentWriteDuration  = new Trend('comment_write_duration');
const commentDeleteDuration = new Trend('comment_delete_duration');
const errorRate             = new Rate('error_rate');
const writeSuccess          = new Counter('comment_write_success');

const MAX_VUS   = 50;
const PAGE_SIZE = 10;

export const options = {
    scenarios: {
        comment_write_ramp: {
            executor: 'ramping-vus',
            startVUs: 1,
            stages: [
                { duration: '10s', target: 10  },
                { duration: '20s', target: 10  },
                { duration: '10s', target: MAX_VUS },
                { duration: '30s', target: MAX_VUS },
                { duration: '10s', target: 0   },
            ],
        },
    },
    thresholds: {
        comment_write_duration:  ['p(95)<500'],
        comment_delete_duration: ['p(95)<500'],
        error_rate:              ['rate<0.05'],
    },
};

export function setup() {
    const sessions = setupSessions(MAX_VUS);

    // 첫 번째 세션으로 게시글 목록 조회 → postId 수집
    const firstSession = sessions.find(s => s.cookieHeader !== null);
    if (!firstSession) {
        console.error('[setup] 로그인된 세션이 없습니다.');
        return { sessions, postIds: [] };
    }

    const feedRes = http.get(
        `${BASE_URL}/api/posts?size=${PAGE_SIZE}`,
        { headers: { Cookie: firstSession.cookieHeader } }
    );

    let postIds = [];
    if (feedRes.status === 200) {
        try {
            const content = feedRes.json('content');
            postIds = content.map(p => p.postId).filter(id => id !== undefined);
            console.log(`[setup] 수집된 postId: ${postIds.join(', ')}`);
        } catch (e) {
            console.error(`[setup] 피드 파싱 실패: ${e}`);
        }
    } else {
        console.error(`[setup] 피드 조회 실패: status=${feedRes.status}`);
    }

    return { sessions, postIds };
}

export default function ({ sessions, postIds }) {
    const session = sessions[__VU % sessions.length];

    if (!session.cookieHeader) {
        errorRate.add(1);
        console.log(`[로그인 세션 없음] VU=${__VU}, userId=${session.userId}`);
        return;
    }

    if (!postIds || postIds.length === 0) {
        errorRate.add(1);
        console.log(`[postId 없음] VU=${__VU} - 게시글이 존재하지 않습니다.`);
        return;
    }

    const headers = {
        'Content-Type': 'application/json',
        Cookie: session.cookieHeader,
    };

    // 랜덤 게시글에 댓글 작성
    const postId = postIds[__ITER % postIds.length];

    // ── 1. 댓글 작성 ─────────────────────────────────────────────────────
    const writeStart = Date.now();
    const writeRes = http.post(
        `${BASE_URL}/api/comment`,
        JSON.stringify({
            postId,
            commentContent: `부하테스트 댓글 - VU${__VU} iter${__ITER}`,
        }),
        { headers }
    );
    commentWriteDuration.add(Date.now() - writeStart);

    const writeOk = check(writeRes, {
        '댓글 작성 성공 (200)': (r) => r.status === 200,
        'commentId 반환':       (r) => {
            try { return r.json('commentId') > 0; } catch { return false; }
        },
    });

    if (!writeOk) {
        errorRate.add(1);
        console.log(`[댓글 작성 실패] VU=${__VU}, userId=${session.userId}, postId=${postId}, status=${writeRes.status}, body=${writeRes.body}`);
        return;
    }
    errorRate.add(0);
    writeSuccess.add(1);

    const commentId = writeRes.json('commentId');
    sleep(0.5);

    // ── 2. 댓글 삭제 (cleanup) ────────────────────────────────────────────
    const deleteStart = Date.now();
    const deleteRes = http.del(
        `${BASE_URL}/api/comment/${commentId}`,
        null,
        { headers }
    );
    commentDeleteDuration.add(Date.now() - deleteStart);

    check(deleteRes, {
        '댓글 삭제 성공 (200)': (r) => r.status === 200,
    });

    sleep(0.3);
}