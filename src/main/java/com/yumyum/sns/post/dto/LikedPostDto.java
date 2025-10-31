package com.yumyum.sns.post.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class LikedPostDto {
    private Long postId;
    private String thumbnailPath;
    private Long likeCount;
    private Long commentCount;
    private LocalDateTime createdAt;

    @QueryProjection
    public LikedPostDto(Long postId, String thumbnailPath, Long likeCount, Long commentCount, LocalDateTime createdAt) {
        this.postId = postId;
        this.thumbnailPath = thumbnailPath;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.createdAt = createdAt;
    }
}
