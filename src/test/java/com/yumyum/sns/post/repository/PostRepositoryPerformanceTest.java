package com.yumyum.sns.post.repository;

import com.yumyum.sns.post.dto.PostCursorRequest;
import com.yumyum.sns.post.dto.PostResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.stream.Collectors;

/**
 * findPagingPosts (JPAExpressions 서브쿼리) vs
 * findPagingPostsWithIn (WHERE IN 배치 조회) 성능 비교 테스트
 *
 * - 실제 MySQL DB에 연결하여 측정 (H2 미사용)
 * - 웜업(WARM_UP회) 후 교차 측정(ROUNDS회)으로 캐시 편향 최소화
 * - 동일한 결과를 반환하는지 postId 목록으로 검증
 */
@SpringBootTest
@Transactional
class PostRepositoryPerformanceTest {

    @Autowired
    private PostRepository postRepository;

    /** JVM JIT 컴파일 안정화를 위한 웜업 횟수 */
    private static final int WARM_UP = 3;

    /** 실제 측정 횟수 */
    private static final int ROUNDS = 10;

    /** 테스트에 사용할 회원 ID (DB에 존재하는 memberId로 변경) */
    private static final Long MEMBER_ID = 400003L;

    // ──────────────────────────────────────────────
    // 테스트 메서드 (pageSize 별)
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("첫 페이지 | pageSize=10 성능 비교")
    public void compareFirstPage_size10() {
        compare(new PostCursorRequest(null, null, 10), MEMBER_ID);
    }

    @Test
    @DisplayName("첫 페이지 | pageSize=20 성능 비교")
    public void compareFirstPage_size20() {
        compare(new PostCursorRequest(null, null, 20), MEMBER_ID);
    }

    @Test
    @DisplayName("첫 페이지 | pageSize=50 성능 비교")
    public void compareFirstPage_size50() {
        compare(new PostCursorRequest(null, null, 50), MEMBER_ID);
    }

    /**
     * 커서가 있는 중간 페이지 테스트.
     * cursorPostId / cursorCreatedAt 값을 DB에 실제 존재하는 값으로 설정해야 의미 있음.
     */
    @Test
    @DisplayName("중간 페이지 | pageSize=20 성능 비교 (커서 있음)")
    public void compareMiddlePage_size20() {
        PostCursorRequest cursor = new PostCursorRequest(
                50L,
                LocalDateTime.of(2025, 12, 31, 20, 6, 33),
                20
        );
        compare(cursor, MEMBER_ID);
    }

    // ──────────────────────────────────────────────
    // 핵심 측정 로직
    // ──────────────────────────────────────────────

    private void compare(PostCursorRequest cursor, Long memberId) {
        printHeader(cursor, memberId);

        // ── 웜업 ──
        System.out.printf("  [웜업] 각 %d회 실행 중...%n", WARM_UP);
        for (int i = 0; i < WARM_UP; i++) {
            postRepository.findPagingPosts(cursor, memberId);
            postRepository.findPagingPostsWithIn(cursor, memberId);
        }

        long[] subqueryMs = new long[ROUNDS];
        long[] inMs       = new long[ROUNDS];

        // ── 교차 측정 (DB 쿼리 캐시 영향을 양쪽에 동일하게 분산) ──
        for (int i = 0; i < ROUNDS; i++) {
            long start = System.nanoTime();
            postRepository.findPagingPosts(cursor, memberId);
            subqueryMs[i] = (System.nanoTime() - start) / 1_000_000;

            start = System.nanoTime();
            postRepository.findPagingPostsWithIn(cursor, memberId);
            inMs[i] = (System.nanoTime() - start) / 1_000_000;
        }

        // ── 결과 동등성 검증 ──
        List<PostResponseDTO> subResult = postRepository.findPagingPosts(cursor, memberId);
        List<PostResponseDTO> inResult  = postRepository.findPagingPostsWithIn(cursor, memberId);
        verifyResults(subResult, inResult);

        // ── 측정 결과 출력 ──
        printResultTable(subqueryMs, inMs);
        printRawTimes(subqueryMs, inMs);
        printConclusion(subqueryMs, inMs);
        System.out.println("  " + "=".repeat(56));
    }

    // ──────────────────────────────────────────────
    // 검증
    // ──────────────────────────────────────────────

    private void verifyResults(List<PostResponseDTO> subResult, List<PostResponseDTO> inResult) {
        List<Long> subIds = subResult.stream().map(PostResponseDTO::getPostId).collect(Collectors.toList());
        List<Long> inIds  = inResult.stream().map(PostResponseDTO::getPostId).collect(Collectors.toList());

        System.out.printf("  조회 결과: 서브쿼리=%d건, WHERE IN=%d건%n", subResult.size(), inResult.size());

        if (subIds.equals(inIds)) {
            System.out.println("  결과 검증: ✔ 두 방식의 postId 목록과 순서가 일치합니다.");
        } else {
            System.out.println("  결과 검증: ✘ postId 목록이 다릅니다. 쿼리 결과를 확인하세요.");
            System.out.println("    서브쿼리: " + subIds);
            System.out.println("    WHERE IN: " + inIds);
        }
    }

    // ──────────────────────────────────────────────
    // 출력 헬퍼
    // ──────────────────────────────────────────────

    private void printHeader(PostCursorRequest cursor, Long memberId) {
        System.out.println();
        System.out.println("  " + "=".repeat(56));
        System.out.printf("  성능 비교 | pageSize=%-2d | memberId=%-3d | 측정=%d회%n",
                cursor.getSize(), memberId, ROUNDS);
        System.out.println("  " + "=".repeat(56));
    }

    private void printResultTable(long[] subqueryMs, long[] inMs) {
        System.out.println("  " + "-".repeat(56));
        System.out.printf("  %-13s %6s ms %6s ms %6s ms%n", "", "avg", "min", "max");
        System.out.println("  " + "-".repeat(56));
        printRow("서브쿼리", subqueryMs);
        printRow("WHERE IN", inMs);
        System.out.println("  " + "-".repeat(56));
    }

    private void printRow(String label, long[] ms) {
        LongSummaryStatistics s = Arrays.stream(ms).summaryStatistics();
        System.out.printf("  [%-9s] %6.1f ms %6d ms %6d ms%n",
                label, s.getAverage(), s.getMin(), s.getMax());
    }

    private void printRawTimes(long[] subqueryMs, long[] inMs) {
        System.out.println("  [원시값 - 서브쿼리] " + Arrays.toString(subqueryMs) + " ms");
        System.out.println("  [원시값 - WHERE IN] " + Arrays.toString(inMs) + " ms");
        System.out.println("  " + "-".repeat(56));
    }

    private void printConclusion(long[] subqueryMs, long[] inMs) {
        double subAvg = Arrays.stream(subqueryMs).average().orElse(1);
        double inAvg  = Arrays.stream(inMs).average().orElse(1);
        boolean inFaster = inAvg < subAvg;
        double ratio = inFaster ? subAvg / inAvg : inAvg / subAvg;
        String winner = inFaster ? "WHERE IN" : "서브쿼리";
        System.out.printf("  결론: [%s] 버전이 평균 %.2f배 빠름%n", winner, ratio);
    }
}
