package com.yumyum.sns.post.entity;

import com.yumyum.sns.attachment.entity.Attachment;
import com.yumyum.sns.comment.entity.Comment;
import com.yumyum.sns.common.BaseTimeEntity;
import com.yumyum.sns.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_ID")
    private Member member;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "ATTACHMENT_ID")
    private Attachment attachment;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Likes> likesList = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostTag> postTags = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> commentList = new ArrayList<>();

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    private String thumbnailPath;

    public Post(String content,String thumbnailPath) {
        this.content = content;
        this.thumbnailPath = thumbnailPath;
    }

    public void setPostRelation(Member member, Attachment attachment){
        this.member = member;
        if(attachment.getId()!=null && attachment.getId()>0){
            this.attachment = attachment;
        }
    }

    public void updateContent(String content) {
        if (content != null && !content.isBlank()) {
            this.content = content;
        }
    }

    public void updateThumbnail(String thumbnailUrl) {
        if (thumbnailUrl != null && !thumbnailUrl.isBlank()) {
            this.thumbnailPath = thumbnailUrl;
        }
    }
}
