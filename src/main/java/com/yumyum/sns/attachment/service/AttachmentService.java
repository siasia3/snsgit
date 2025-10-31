package com.yumyum.sns.attachment.service;

import com.yumyum.sns.attachment.dto.AttachDto;
import com.yumyum.sns.attachment.dto.AttachwithDetailDto;
import com.yumyum.sns.attachment.dto.ThumbnailResponse;
import com.yumyum.sns.attachment.entity.Attachment;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface AttachmentService {

    /**
     * 첨부파일 생성
     * @param files 생성할 첨부파일들
     * @return 썸네일 url 및 파일 정보가 포함된 DTO
     */
    ThumbnailResponse createAttachment(List<MultipartFile> files);


    /**
     * 여러 개의 게시글 첨부파일을 조회
     * @param attachIds
     * @return postId를 key, 첨부파일 dto 리스트를 value로 가진 map
     */
    Map<Long, List<AttachDto>> getAttachmentsByPost(List<Long> attachIds);

    /**
     * 특정 게시물의 첨부파일 조회
     * @param postId 게시글 PK
     * @return 단건 게시글의 첨부파일들
     */
    List<AttachwithDetailDto> getAttachmentByPostDetail(Long postId);


    /**
     * 세부 첨부파일 삭제
     * @param attachmentId 첨부파일 PK
     */
    void deleteAttachmentDetail(Long attachmentId);

    /**
     * 첨부파일 수정
     * @param attachmentId 첨부파일 PK
     * @param files 등록할 첨부파일
     * @return 수정된 첨부파일 썸네일 및 파일정보 DTO
     */
    ThumbnailResponse updateAttachment(Long attachmentId, List<MultipartFile> files);

}
