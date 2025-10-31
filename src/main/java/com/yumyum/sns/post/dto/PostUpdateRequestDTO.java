package com.yumyum.sns.post.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PostUpdateRequestDTO {

    private Long postId;
    private String postContent;
    private List<TagDto> hashtags;
    private Long attachmentId;
}
