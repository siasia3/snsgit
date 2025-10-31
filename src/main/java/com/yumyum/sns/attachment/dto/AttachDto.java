package com.yumyum.sns.attachment.dto;

import com.yumyum.sns.attachment.entity.Attachment;
import com.yumyum.sns.attachment.entity.AttachmentDetail;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class AttachDto {
    private String originalFileName;
    private String savedFileName;
    private String path;
    private Long size;
    private String type;
    private Long postId;
    private Long attachmentId;
    private Long attachDetailId;

    public AttachDto(Long attachmentId,Long attachDetailId,Long postId, String path,String type){
        this.attachmentId = attachmentId;
        this.attachDetailId = attachDetailId;
        this.postId = postId;
        this.path = path;
        this.type = type;
    }

    public AttachDto(MultipartFile file, String fileName,String path) {
        this.originalFileName = file.getOriginalFilename();
        this.savedFileName = fileName;
        this.path = path;
        this.size = file.getSize();
        this.type = file.getContentType();
    }

    public AttachmentDetail toEntity(Attachment attachment) {
        return AttachmentDetail.builder()
                .originalFileName(this.originalFileName)
                .savedFileName(this.savedFileName)
                .path(this.path)
                .size(this.size)
                .type(this.type)
                .attachment(attachment)
                .build();
    }


}
