package com.yumyum.sns.post.service;

import com.yumyum.sns.attachment.dto.AttachDto;
import com.yumyum.sns.attachment.dto.AttachwithDetailDto;
import com.yumyum.sns.attachment.dto.ThumbnailResponse;
import com.yumyum.sns.attachment.entity.Attachment;
import com.yumyum.sns.attachment.service.AttachmentService;
import com.yumyum.sns.comment.service.CommentService;
import com.yumyum.sns.infra.StorageService;
import com.yumyum.sns.member.entity.Member;
import com.yumyum.sns.member.service.MemberService;
import com.yumyum.sns.post.dto.*;
import com.yumyum.sns.post.entity.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostFacadeServiceImpl implements PostFacadeService{

    private final MemberService memberService;
    private final PostService postService;
    private final TagService tagService;
    private final AttachmentService attachmentService;
    private final StorageService storageService;
    private final PostTransactionService postTransactionService;

    // 파일, 게시판, 해시태그 생성
    @Override
    public Long registerPost(PostRequestDto postRequestDto, List<MultipartFile> files, String identifier) {
        Member checkMember = memberService.getMemberByIdentifier(identifier);
        //storage 업로드
        List<AttachDto> attachDtos = storageService.uploadFiles(files);

        //게시글 등록
        Long postId = postTransactionService.savePost(postRequestDto, attachDtos, checkMember);
        return postId;
    }

    //파일,게시판,해시태그 수정
    @Override
    @Transactional
    public PostUpdateResponseDTO modifyPost(PostUpdateRequestDTO postUpdateRequestDto, List<MultipartFile> files, String identifier) {
        Member checkMember = memberService.getMemberByIdentifier(identifier);
        List<TagDto> hashtags = postUpdateRequestDto.getHashtags();
        Optional<ThumbnailResponse> attachment = Optional.empty();

        //첨부파일 update
        if(files != null && !files.isEmpty()){
             attachment = Optional.of(attachmentService.updateAttachment(postUpdateRequestDto.getAttachmentId(), files));
        }

        //게시글 update
        Post post = postService.updatePost(postUpdateRequestDto, checkMember, attachment);

        //해시태그 update
        if(hashtags != null && !hashtags.isEmpty()){
            for(TagDto hashtag : hashtags){
                tagService.updateTag(hashtag,post);
            }
        }

        return new PostUpdateResponseDTO(post);
    }


    //게시글 과 게시글 관련 정보 페이징 조회
    @Override
    @Cacheable(value = "feed", key = "'firstPage'", condition = "#cursor.cursorPostId == null")
    @Transactional(readOnly = true)
    public PostSliceDto getPostsWithInfo(PostCursorRequest cursor, Long memberId) {

        List<PostResponseDTO> pagingPosts = postService.getPagingPosts(cursor, memberId);

        List<Long> attachIds = pagingPosts.stream().map(o -> o.getAttachmentId()).toList();

        Map<Long, List<AttachDto>> attachListMap = attachmentService.getAttachmentsByPost(attachIds);

        List<PostResponseDTO> postListWithInfo = pagingPosts.stream().map(post -> {

            List<AttachDto> attachDtos = attachListMap.get(post.getPostId());

            post.setAttachments(Optional.ofNullable(attachDtos).orElseGet(ArrayList::new));

            return post;
        }).collect(Collectors.toList());

        return new PostSliceDto(postListWithInfo,cursor.getSize());
    }

}
