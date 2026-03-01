package com.yumyum.sns.post.service;

import com.yumyum.sns.post.dto.*;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PostFacadeService {

    /**
     * 파일생성,게시글생성,해시태그생성 서비스를 호출
     * @param postRequestDto 등록할 게시글 DTO
     * @param files 등록할 첨부파일
     * @param identifier 회원 식별자
     * @return 게시글 pk
     */
    Long registerPost(PostRequestDto postRequestDto, List<MultipartFile> files, String identifier);

    /**
     * 파일수정,게시글수정,해시태그수정 서비스를 호출
     * @param postUpdateRequestDto 수정할 게시글 DTO
     * @param files 수정할 파일
     * @param identifier 회원 식별자
     * @return 수정된 게시글
     */
    PostUpdateResponseDTO modifyPost(PostUpdateRequestDTO postUpdateRequestDto, List<MultipartFile> files, String identifier);

    /**
     * 게시글 과 게시글 관련 정보 페이징 조회
     * @param cursor 페이징
     * @param memberId 회원 PK
     * @return 페이징 조회한 게시글
     */
    PostSliceDto getPostsWithInfo(PostCursorRequest cursor, Long memberId);

}
