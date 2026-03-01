package com.yumyum.sns.post.service;


import com.yumyum.sns.attachment.dto.AttachwithDetailDto;
import com.yumyum.sns.attachment.dto.ThumbnailResponse;
import com.yumyum.sns.attachment.service.AttachmentService;
import com.yumyum.sns.error.exception.PostNotFoundException;
import com.yumyum.sns.member.entity.Member;
import com.yumyum.sns.member.service.MemberService;
import com.yumyum.sns.post.dto.*;
import com.yumyum.sns.post.entity.Post;
import com.yumyum.sns.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class PostServiceImpl implements PostService{

    private final PostRepository postRepository;
    private final MemberService memberService;
    private final AttachmentService attachmentService;


    //게시글 작성
    @Override
    public Post createPost(PostRequestDto postRequestDto,Member member, ThumbnailResponse createdAttach) {
        Post post = new Post(postRequestDto.getPostContent(), createdAttach.getThumbnailPath());
        post.setPostRelation(member,createdAttach.getAttachment());
        return postRepository.save(post);
    }

    //게시글ID로 조회 겸 검증
    @Override
    @Transactional(readOnly = true)
    public Post getPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
    }

    //게시글 수정
    @Override
    public Post updatePost(PostUpdateRequestDTO postUpdateRequestDTO,Member checkMember,Optional<ThumbnailResponse> attachment){
        Post post = getPostById(postUpdateRequestDTO.getPostId());
        Long authorId = post.getMember().getId();
        Long checkMemberId = checkMember.getId();
        if(authorId != checkMemberId){
            throw new AccessDeniedException("게시글 작성자가 아닙니다. memberId: "+ checkMemberId);
        }

        //게시글 내용을 수정한 경우
        if (postUpdateRequestDTO.getPostContent() != null) {
            post.updateContent(postUpdateRequestDTO.getPostContent());
        }
        //첨부파일을 수정한 경우
        if (attachment.isPresent()) {
            post.updateThumbnail(attachment.get().getThumbnailPath());
        }

        return post;
    }
    
    //게시글 삭제
    @Override
    public void deletePost(Long postId,String identifier) {
        Member member = memberService.getMemberByIdentifier(identifier);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        if(!post.getMember().getId().equals(member.getId())){
           throw new AccessDeniedException("게시글 작성자가 아닙니다. memberId: "+ member.getId());
        }
        //storage와 detail 삭제
        attachmentService.deleteAttachmentDetail(post.getAttachment().getId());
        //게시글 entity 삭제
        postRepository.delete(post);
    }

    
    //게시글 목록 조회
    @Override
    @Transactional(readOnly = true)
    public List<PostResponseDTO> getPagingPosts(PostCursorRequest postCursorRequest, Long memberId) {

        return postRepository.findPagingPosts(postCursorRequest,memberId);
    }

    //게시글 상세 조회
    @Override
    @Transactional(readOnly = true)
    public PostDetailDto getPostDetail(Long postId, Long memberId) {

        return Optional.ofNullable(postRepository.findPostDetail(postId, memberId))
                .orElseThrow(() -> new PostNotFoundException(postId));
    }

    //회원이 작성한 게시글 조회
    @Override
    @Transactional(readOnly = true)
    public MemberPostPageDto getMemberPosts(Pageable pageable, String nickName) {
        Member member = memberService.getMemberByNickname(nickName);
        List<MemberPostDto> memberPosts = postRepository.findMemberPosts(pageable, member.getId());
        return new MemberPostPageDto(memberPosts,pageable);
    }

    //게시글과 첨부파일 상세조회
    @Override
    @Transactional(readOnly = true)
    public PostDetailDto getPostDetailWithInfo(Long memberId, Long postId) {
        PostDetailDto postDetail = getPostDetail(postId, memberId);
        List<AttachwithDetailDto> attachment = attachmentService.getAttachmentByPostDetail(postId);
        postDetail.setAttachments(attachment);
        return postDetail;
    }

    //회원 좋아요 게시글 조회
    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse getLikedPosts(int pageSize, LocalDateTime cursorCreatedAt, Long memberId) {
        memberService.getMemberById(memberId);
        List<LikedPostDto> likedPosts = postRepository.findLikedPosts(pageSize, cursorCreatedAt, memberId);
        LocalDateTime nextCursor = likedPosts.size() > pageSize
                ? likedPosts.get(pageSize).getCreatedAt()
                : null;
        return new CursorPageResponse(likedPosts,nextCursor);
    }

    //게시글 첨부파일 조회
    @Override
    @Transactional(readOnly = true)
    public PostDTO getPost(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException(postId));
        List<AttachwithDetailDto> attachment = attachmentService.getAttachmentByPostDetail(postId);

        return new PostDTO(post,attachment);
    }

}
