package com.yumyum.sns.post.service;

import com.yumyum.sns.attachment.dto.AttachDto;
import com.yumyum.sns.attachment.dto.ThumbnailResponse;
import com.yumyum.sns.attachment.service.AttachmentService;
import com.yumyum.sns.member.entity.Member;
import com.yumyum.sns.post.dto.PostRequestDto;
import com.yumyum.sns.post.dto.TagDto;
import com.yumyum.sns.post.entity.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostTransactionService {

    private final PostService postService;
    private final TagService tagService;
    private final AttachmentService attachmentService;

    @Transactional
    public Long savePost(PostRequestDto postRequestDto, List<AttachDto> fileNames, Member checkdMember){


        List<TagDto> hashtags = postRequestDto.getHashtags();

        //첨부파일 insert
        ThumbnailResponse createdAttach = attachmentService.createAttachment(fileNames);
        //게시판 insert
        Post post = postService.createPost(postRequestDto,checkdMember,createdAttach);

        //해시태그가 있는 경우 insert
        if(hashtags != null && !hashtags.isEmpty()){
            for(TagDto hashtag : hashtags){
                tagService.createTag(hashtag,post);
            }
        }

        return post.getId();
    }


}
