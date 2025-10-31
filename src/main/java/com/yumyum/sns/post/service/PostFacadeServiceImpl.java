package com.yumyum.sns.post.service;

import com.yumyum.sns.attachment.dto.AttachDto;
import com.yumyum.sns.attachment.dto.AttachwithDetailDto;
import com.yumyum.sns.attachment.dto.ThumbnailResponse;
import com.yumyum.sns.attachment.entity.Attachment;
import com.yumyum.sns.attachment.service.AttachmentService;
import com.yumyum.sns.comment.service.CommentService;
import com.yumyum.sns.member.entity.Member;
import com.yumyum.sns.member.service.MemberService;
import com.yumyum.sns.post.dto.*;
import com.yumyum.sns.post.entity.Post;
import lombok.RequiredArgsConstructor;
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
@Transactional
@RequiredArgsConstructor
public class PostFacadeServiceImpl implements PostFacadeService{

    private final MemberService memberService;
    private final PostService postService;
    private final TagService tagService;
    private final AttachmentService attachmentService;

    // 파일, 게시판, 해시태그 생성
    @Override
    public void registerPost(PostRequestDto postRequestDto, List<MultipartFile> files, String identifier) {
        Member checkMember = memberService.getMemberByIdentifier(identifier);
        Attachment attachment = new Attachment();
        List<TagDto> hashtags = postRequestDto.getHashtags();

        //첨부파일 insert
        ThumbnailResponse createdAttach = attachmentService.createAttachment(files);
        //게시판 insert
        Post post = postService.createPost(postRequestDto,checkMember,createdAttach);

        //해시태그가 있는 경우 insert
        if(hashtags != null && !hashtags.isEmpty()){
            for(TagDto hashtag : hashtags){
                tagService.createTag(hashtag,post);
            }
        }
    }

    //파일,게시판,해시태그 수정
    @Override
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
    public PostSliceDto getPostsWithInfo(Pageable pageable, Long memberId) {
        //toOne 관계 페이징조회
        List<PostResponseDTO> pagingPosts = postService.getPagingPosts(pageable, memberId);

        // 1:N관계를 처리하기 위해 키값을 뽑아서 리스트 변환 -> 뽑은 키 값을 통해서 조회해서 N관계를 리스트 반환
        //List<Long> postIds = pagingPosts.stream().map(o -> o.getPostId()).toList();
        List<Long> attachIds = pagingPosts.stream().map(o -> o.getAttachmentId()).toList();

        //ID(키)와 DTO값(value)을 Map으로 변환
        Map<Long, List<AttachDto>> attachListMap = attachmentService.getAttachmentsByPost(attachIds);
        //Map<Long, CommentCntDto> totalCommentCntMap = commentService.getCommentCntsByPostIds(postIds);


        //
        List<PostResponseDTO> postListWithInfo = pagingPosts.stream().map(post -> {
            //CommentCntDto commentCnts = totalCommentCntMap.get(post.getPostId()); // 댓글 수
            List<AttachDto> attachDtos = attachListMap.get(post.getPostId()); // 첨부파일 리스트


            //post.setCommentCount(Optional.ofNullable(commentCnts).map(CommentCntDto::getTotalCommentCnt).orElse(0L));
            post.setAttachments(Optional.ofNullable(attachDtos).orElseGet(ArrayList::new));


            return post;
        }).collect(Collectors.toList());

        //hasNext값을 넣어주기 위해서 DTO로 감싸 넣어줌
        return new PostSliceDto(postListWithInfo,pageable);
    }

}
