package com.yumyum.sns.comment.dto;

import com.yumyum.sns.comment.entity.Comment;
import com.yumyum.sns.member.entity.Member;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CommentResponseDto {

    private Long commentId;
    private String commentAuthor;
    private String commentContent;
    private String authorProfileImage;
    private Long parentId;
    private Long authorId;

    public CommentResponseDto(Member author, Comment comment) {
        this.commentAuthor = author.getName();
        this.authorProfileImage = author.getProfileImage();
        this.authorId = author.getId();
        this.commentContent = comment.getContent();
        this.commentId = comment.getId();
        if(comment.getParent() != null) {
            this.parentId = comment.getParent().getId();
        }
    }
}
