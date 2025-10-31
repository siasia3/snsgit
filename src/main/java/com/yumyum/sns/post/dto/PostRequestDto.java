package com.yumyum.sns.post.dto;

import com.yumyum.sns.attachment.entity.Attachment;
import com.yumyum.sns.member.entity.Member;
import com.yumyum.sns.post.entity.Post;
import com.yumyum.sns.post.entity.Tag;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PostRequestDto {

    private Long id;
    private int likeCount;
    private String postContent;
    private List<TagDto> hashtags;
}
