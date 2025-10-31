package com.yumyum.sns.comment.controller;

import com.yumyum.sns.comment.dto.*;
import com.yumyum.sns.comment.service.CommentService;
import com.yumyum.sns.oauthjwt.dto.CustomOAuth2User;
import com.yumyum.sns.oauthjwt.jwt.JWTUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CommentController {

    private final JWTUtil jwtUtil;
    private final CommentService commentService;


    @PostMapping(value = "/comment")
    public ResponseEntity<CommentResponseDto> createComment(
                                        @CookieValue(name = "Authorization") String jwt,
                                        @Valid @RequestBody CommentRequestDto commentRequestDto){

        String identifier = jwtUtil.getUsername(jwt);
        CommentResponseDto comment = commentService.createComment(commentRequestDto, identifier);

        return ResponseEntity.ok(comment);
    }

    @GetMapping(value = "/post/{postId}/comments")
    public CommentSliceDto getComments(Pageable pageable,
                                       @PathVariable Long postId){
        CommentSliceDto commentsByPost = commentService.getCommentsByPost(pageable, postId);
        return commentsByPost;
    }

    @GetMapping(value = "/comment/{parentId}/replies")
    public ResponseEntity<ReplySliceDto> getReplies(Pageable pageable,
                                                     @PathVariable Long parentId){
        ReplySliceDto replies = commentService.getRepliesByComment(pageable, parentId);
        return ResponseEntity.ok(replies);
    }

    @DeleteMapping("/comment/{commentId}")
    public ResponseEntity<Map<String,String>> deleteComment(Authentication authentication,
                                                            @PathVariable Long commentId){
        String identifier = authentication.getName();
        commentService.deleteComment(commentId,identifier);

        return ResponseEntity.ok(Map.of("message","ok"));
    }



}
