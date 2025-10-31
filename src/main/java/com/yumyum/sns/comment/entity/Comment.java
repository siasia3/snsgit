package com.yumyum.sns.comment.entity;

import com.yumyum.sns.comment.dto.CommentRequestDto;
import com.yumyum.sns.common.BaseTimeEntity;
import com.yumyum.sns.member.entity.Member;
import com.yumyum.sns.post.entity.Post;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent; // 부모 댓글 (대댓글일 경우)

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> replyList = new ArrayList<>();

    @Column(nullable = false, length = 2000)
    private String content;

    public Comment(Post post, Member member, String commentContent) {
        this.post = post;
        this.member = member;
        this.content = commentContent;
    }

    // 대댓글 부모댓글 set
    public void setParentId(Comment parentComment){
        this.parent = parentComment;
    }
}
