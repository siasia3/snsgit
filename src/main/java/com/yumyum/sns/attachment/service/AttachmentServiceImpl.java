package com.yumyum.sns.attachment.service;

import com.yumyum.sns.attachment.dto.AttachDto;
import com.yumyum.sns.attachment.dto.AttachwithDetailDto;
import com.yumyum.sns.attachment.dto.ThumbnailResponse;
import com.yumyum.sns.attachment.entity.Attachment;
import com.yumyum.sns.attachment.entity.AttachmentDetail;
import com.yumyum.sns.attachment.repository.AttachmentDetailRepository;
import com.yumyum.sns.attachment.repository.AttachmentRepository;
import com.yumyum.sns.error.exception.AttachmentNotFoundException;
import com.yumyum.sns.infra.RollbackManager;
import com.yumyum.sns.infra.StorageService;
import com.yumyum.sns.infra.service.StorageDeleteOutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {

    private final RollbackManager rollbackManager;
    private final StorageService storageService;
    private final StorageDeleteOutboxService storageDeleteOutboxService;
    private final AttachmentRepository attachmentRepository;
    private final AttachmentDetailRepository attachmentDetailRepository;

    //Attachment와 AttachmentDetail insert
    @Override
    @Transactional
    public ThumbnailResponse createAttachment(List<AttachDto> attachDtos) {

        //롤백시 storage 정리
        rollbackManager.deleteIfTransactionRollback(storageService.toSavedFileNames(attachDtos));

        Attachment attach = attachmentRepository.save(new Attachment());
        for (AttachDto attachDto : attachDtos) {
            attachmentDetailRepository.save(attachDto.toEntity(attach));
        }

        return new ThumbnailResponse(attach, attachDtos.get(0).getPath());

    }


    //게시글 목록 조회시 첨부파일 가져오기
    @Override
    @Transactional(readOnly = true)
    public Map<Long, List<AttachDto>> getAttachmentsByPost(List<Long> attachIds) {
        List<AttachDto> attachList = attachmentRepository.findAttachmentsByPost(attachIds);
        //ID(postId키)와 DTO값(value)을 Map으로 변환
        Map<Long, List<AttachDto>> attachListMap = attachList.stream().collect(Collectors.groupingBy(attach -> attach.getPostId()));
        return attachListMap;
    }

    //게시글 상세 조회시 첨부파일 가져오기
    @Override
    @Transactional(readOnly = true)
    public List<AttachwithDetailDto> getAttachmentByPostDetail(Long postId) {
        List<AttachwithDetailDto> attachment = attachmentRepository.findAttachmentByPostDetail(postId);
        if (attachment.isEmpty()) {
            throw new AttachmentNotFoundException("해당 게시글의 첨부파일을 조회하지 못했습니다.");
        }
        return attachment;
    }

    //첨부파일 상세 삭제
    @Override
    @Transactional
    public void deleteAttachmentDetail(Long attachmentId) {
        List<AttachmentDetail> attachments = attachmentDetailRepository.findByAttachmentId(attachmentId);
        if(attachments.isEmpty()){
            throw new AttachmentNotFoundException("잘못된 첨부파일 ID: " + attachmentId);
        }

        for (AttachmentDetail attachment : attachments) {
            //db delete를 먼저 해야 예외가 터져도 storage deleteFile까지 안함
            attachmentDetailRepository.delete(attachment);
            if (!storageService.deleteFile(attachment.getSavedFileName())) {
                storageDeleteOutboxService.save(attachment.getSavedFileName());
            }
        }
    }

    @Override
    @Transactional
    public ThumbnailResponse updateAttachment(Long attachmentId, List<MultipartFile> files) {

        //storage 업로드
        List<AttachDto> attachDtos = storageService.uploadFiles(files);
        //롤백시 storage 정리
        rollbackManager.deleteIfTransactionRollback(storageService.toSavedFileNames(attachDtos));

        //삭제 후 등록하는 방식으로 수정
        deleteAttachmentDetail(attachmentId);

        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new AttachmentNotFoundException("잘못된 첨부파일 ID: " + attachmentId));

        for (AttachDto attachDto : attachDtos) {
            attachmentDetailRepository.save(attachDto.toEntity(attachment));
        }

        return new ThumbnailResponse(attachment, attachDtos.get(0).getPath());

    }


}
