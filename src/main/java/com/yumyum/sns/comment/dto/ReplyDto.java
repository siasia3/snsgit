package com.yumyum.sns.comment.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
public class ReplyDto {
    private Long commentId;
    private String commentAuthor;
    private String commentContent;
    private Long authorId;
    private String authorProfileImage;
    private LocalDateTime createdAt;
    private Long parentId;

    @QueryProjection
    public ReplyDto(Long commentId, String commentAuthor, String commentContent, Long authorId, String authorProfileImage, LocalDateTime createdAt, Long parentId) {
        this.commentId = commentId;
        this.commentAuthor = commentAuthor;
        this.commentContent = commentContent;
        this.authorId = authorId;
        this.authorProfileImage = authorProfileImage;
        this.createdAt = createdAt;
        this.parentId = parentId;
    }
}
