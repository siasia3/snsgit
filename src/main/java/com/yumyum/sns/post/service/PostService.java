package com.yumyum.sns.post.service;

import com.yumyum.sns.attachment.dto.ThumbnailResponse;
import com.yumyum.sns.member.entity.Member;
import com.yumyum.sns.post.dto.*;
import com.yumyum.sns.post.entity.Post;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PostService {

    /**
     * 게시글 작성
     * @param postRequestDto 작성된 게시글 DTO
     * @param member 회원 엔티티
     * @param attachment 게시글 첨부파일 DTO
     * @return 작성한 게시글 엔티티
     */
    Post createPost(PostRequestDto postRequestDto, Member member, ThumbnailResponse attachment);// 게시물 단건 조회

    /**
     * 게시글 단건 조회
     * @param postId 게시글ID
     * @return 조회된 게시글
     */
    Post getPostById(Long postId);

    /**
     * 게시글 수정
     * @param postUpdateRequestDTO 수정할 게시글 DTO
     * @param checkMember 작성자인지 확인할 회원
     * @param attachment 게시글 첨부파일 DTO
     * @return 수정된 게시글 엔티티
     */
    Post updatePost(PostUpdateRequestDTO postUpdateRequestDTO, Member checkMember, Optional<ThumbnailResponse> attachment);



    /**
     * 게시물 삭제
     * @param postId 게시글 PK
     * @param identifier 회원 식별자
     */
    void deletePost(Long postId,String identifier);

    /**
     * 게시글 페이징 조회
     * @param cursor cursor 페이징
     * @param memberId 좋아요 상태를 확인할 회원ID
     * @return 게시글 목록
     */
    List<PostResponseDTO> getPagingPosts(PostCursorRequest cursor, Long memberId);

    /**
     * 게시글 상세 조회
     * @param postId 게시글ID
     * @param memberId 회원ID
     * @return 상세 조회할 게시글
     */
    PostDetailDto getPostDetail(Long postId, Long memberId);

    /**
     * 회원 게시글 조회
     * @param pageable 페이징
     * @param nickName 회원 닉네임
     * @return 회원이 작성한 게시글
     */
    MemberPostPageDto getMemberPosts(Pageable pageable, String nickName);

    /**
     * 게시글,게시글 파일 상세 조회
     * @param memberId 회원ID
     * @param postId 게시글ID
     * @return 상세 조회할 게시글과 첨부파일
     */
    PostDetailDto getPostDetailWithInfo(Long memberId,Long postId);

    /**
     * 회원 좋아요 게시글 조회
     * @param pageSize 페이징 크기
     * @param cursorCreatedAt 커서 페이징 기준(좋아요를 누른 시간)
     * @param memberId 회원ID
     * @return 좋아요 누른 게시글
     */
    CursorPageResponse getLikedPosts(int pageSize, LocalDateTime cursorCreatedAt, Long memberId);

    /**
     * 게시글,첨부파일 조회
     * @param postId 게시글 PK
     * @return 게시글,첨부파일
     */
    PostDTO getPost(Long postId);

}
