package com.yumyum.sns.post.dto;

import com.yumyum.sns.attachment.dto.AttachwithDetailDto;
import com.yumyum.sns.post.entity.Post;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PostDTO {

    private Long postId;
    private String postContent;
    private LocalDateTime createdAt;
    private String thumbnailPath;
    private List<AttachwithDetailDto> attachments;

    public PostDTO(Post post, List<AttachwithDetailDto> attachments) {
        this.postId = post.getId();
        this.postContent = post.getContent();
        this.createdAt = post.getCreatedAt();
        this.thumbnailPath = post.getThumbnailPath();
        this.attachments = attachments;
    }
}
