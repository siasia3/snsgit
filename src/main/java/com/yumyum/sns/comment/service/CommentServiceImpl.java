package com.yumyum.sns.comment.service;

import com.yumyum.sns.comment.dto.*;
import com.yumyum.sns.comment.entity.Comment;
import com.yumyum.sns.comment.repository.CommentRepository;
import com.yumyum.sns.error.exception.CommentNotFoundException;
import com.yumyum.sns.member.entity.Member;
import com.yumyum.sns.member.service.MemberService;
import com.yumyum.sns.post.entity.Post;
import com.yumyum.sns.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService{

    private final MemberService memberService;
    private final PostService postService;
    private final CommentRepository commentRepository;


    //댓글 등록
    @Override
    public CommentResponseDto createComment(CommentRequestDto commentRequestDto, String identifier) {
        Member member = memberService.getMemberByIdentifier(identifier);
        Post post = postService.getPostById(commentRequestDto.getPostId());
        String commentContent = commentRequestDto.getCommentContent();
        Comment comment = new Comment(post,member, commentContent);
        Long parentId = commentRequestDto.getParentId();
        if(parentId != null){
            Comment parentComment = commentRepository.findById(commentRequestDto.getParentId()).orElseThrow(() -> new CommentNotFoundException(parentId));
            comment.setParentId(parentComment);
        }
        Comment savedComment = commentRepository.save(comment);

        return new CommentResponseDto(member, savedComment);
    }

    //게시글 리스트의 각 댓글개수
    @Override
    public Map<Long, CommentCntDto> getCommentCntsByPostIds(List<Long> postIds) {
        List<CommentCntDto> totalCommentCnts = commentRepository.findCommentCntsByPostIds(postIds);
        Map<Long, CommentCntDto> totalCommentCntMap = totalCommentCnts.stream().collect(Collectors.toMap(CommentCntDto::getPostId, comment -> comment));
        return totalCommentCntMap;
    }

    //특정 게시글의 댓글조회
    @Override
    public CommentSliceDto getCommentsByPost(Pageable pageable, Long postId) {
        List<CommentDto> commentsByPost = commentRepository.findCommentsByPost(pageable, postId);
        return new CommentSliceDto(commentsByPost,pageable);
    }

    //댓글 삭제
    @Override
    public Long deleteComment(Long commentId, String identifier) {
        Comment comment = commentRepository.findByIdWithMember(commentId).orElseThrow(() -> new CommentNotFoundException(commentId));
        Member member = memberService.getMemberByIdentifier(identifier);
        if(comment.getMember().getId() != member.getId()){
            throw new IllegalArgumentException("댓글 삭제 권한이 없습니다.");
        }
        commentRepository.delete(comment);
        return comment.getId();
    }

    //대댓글 조회
    @Override
    public ReplySliceDto getRepliesByComment(Pageable pageable, Long parentId) {
        List<ReplyDto> replies = commentRepository.findRepliesByComment(pageable, parentId);

        if (replies.isEmpty()) {
            throw new CommentNotFoundException("해당 parentId를 가진 부모댓글이 존재하지 않습니다. " + parentId);
        }
        return new ReplySliceDto(replies,pageable);
    }

}
