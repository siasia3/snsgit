package com.yumyum.sns.attachment.entity;

import com.yumyum.sns.common.BaseTimeEntity;
import com.yumyum.sns.post.entity.Post;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class Attachment extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "attachment")
    private List<AttachmentDetail> attachments = new ArrayList<>();

    @OneToOne(mappedBy = "attachment", fetch = FetchType.LAZY)
    private Post post;

}
