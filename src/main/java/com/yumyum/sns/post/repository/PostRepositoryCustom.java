package com.yumyum.sns.post.repository;

import com.yumyum.sns.member.entity.Member;
import com.yumyum.sns.post.dto.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRepositoryCustom {

    /**
     * 게시글 페이징조회
     * @param cursor cursor 페이징
     * @param memberId 회원ID
     * @return 조회된 게시글들
     */
    List<PostResponseDTO> findPagingPosts(PostCursorRequest cursor, Long memberId);

    /**
     * 게시글 상세조회
     * @param postId 게시글ID
     * @param memberId 회원ID
     * @return 상세조회할 게시글
     */
    PostDetailDto findPostDetail(Long postId, Long memberId);



    /**
     * 회원 게시글 조회
     * @param pageable 페이징
     * @param memberId 회원ID
     * @return 특정 회원이 작성한 게시글들
     */
    List<MemberPostDto> findMemberPosts(Pageable pageable, Long memberId);

    /**
     * 회원 좋아요 게시글 조회
     * @param pageSize 페이징 크기
     * @param cursorCreatedAt 커서 페이징 기준(좋아요 누른 시간)
     * @param memberId 회원ID
     * @return 특정 회원이 좋아요한 게시글들
     */
    List<LikedPostDto> findLikedPosts(int pageSize, LocalDateTime cursorCreatedAt, Long memberId);



    /**
     * 게시글 페이징조회 (WHERE IN 배치 조회 버전)
     * @param cursor cursor 페이징
     * @param memberId 회원ID
     * @return 조회된 게시글들
     */
    List<PostResponseDTO> findPagingPostsWithIn(PostCursorRequest cursor, Long memberId);

    //총 게시글 개수 조회
    Long countTotalPosts();
}
