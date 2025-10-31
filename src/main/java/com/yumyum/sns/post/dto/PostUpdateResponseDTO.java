package com.yumyum.sns.post.dto;


import com.yumyum.sns.attachment.dto.ThumbnailResponse;
import com.yumyum.sns.post.entity.Post;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
public class PostUpdateResponseDTO {
    private Long postId;
    private Long memberId;
    private String postContent;
    private LocalDateTime createdAt;
    private String thumbnailPath;
    private List<TagDto> hashtags;
    private Long attachmentId;


    public PostUpdateResponseDTO(Post post) {
        this.postId = post.getId();
        this.memberId = post.getMember().getId();
        this.postContent = post.getContent();
        this.createdAt = post.getCreatedAt();
        this.thumbnailPath = post.getThumbnailPath();
    }
}
