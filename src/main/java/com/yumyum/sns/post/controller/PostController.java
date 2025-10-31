package com.yumyum.sns.post.controller;

import com.yumyum.sns.error.exception.ApiResponse;
import com.yumyum.sns.member.entity.Member;
import com.yumyum.sns.member.service.MemberService;
import com.yumyum.sns.oauthjwt.dto.CustomOAuth2User;
import com.yumyum.sns.oauthjwt.jwt.JWTUtil;
import com.yumyum.sns.post.dto.*;
import com.yumyum.sns.post.service.PostFacadeService;
import com.yumyum.sns.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PostController {

    private final PostFacadeService postFacadeService;
    private final PostService postService;
    private final MemberService memberService;
    private final JWTUtil jwtUtil;

    //게시글 등록
    @PostMapping(value = "/post", consumes = "multipart/form-data")
    public ResponseEntity<Map<String,String>> createPost(@CookieValue(name = "Authorization") String jwt,
                                        @RequestPart(value= "postContent", required = false) PostRequestDto postRequestDto,
                                        @RequestPart(value = "files") List<MultipartFile> files){

        boolean hasFiles = files != null && !files.isEmpty();
        if(!hasFiles){
            return ResponseEntity.badRequest().body(Map.of("message","파일은 반드시 포함해야 합니다."));
        }
        boolean hasInvalid = files.stream()
                .map(MultipartFile::getContentType)
                .anyMatch(type -> !type.startsWith("image/") && !type.startsWith("video/"));

        if (hasInvalid) {
            return ResponseEntity.badRequest().body(Map.of("message","이미지나 동영상 파일이 아닙니다."));
        }

        String identifier = jwtUtil.getUsername(jwt);
        postFacadeService.registerPost(postRequestDto,files,identifier);

        return ResponseEntity.ok(Map.of("message","게시글이 정상적으로 작성되었습니다."));
    }

    //게시글 수정
    @PatchMapping(value = "/post/{postId}", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<Void>> updatePost(Authentication authentication,
                                             @PathVariable Long postId,
                                             @RequestPart(value="post") PostUpdateRequestDTO updateRequestDTO,
                                             @RequestPart(value = "files", required = false) List<MultipartFile> files){

        String identifier = authentication.getName();
        postFacadeService.modifyPost(updateRequestDTO,files,identifier);
        return ResponseEntity.ok(ApiResponse.success("게시물이 성공적으로 수정되었습니다."));
    }

    //게시글 삭제
    @DeleteMapping(value="/post/{postId}")
    public ResponseEntity<Map<String,String>> deletePost(Authentication authentication,
                                             @PathVariable Long postId){
        String identifier = authentication.getName();
        postService.deletePost(postId,identifier);
        return ResponseEntity.ok(Map.of("message","게시글이 정상적으로 삭제되었습니다."));
    }


    //게시글 목록 조회
    @GetMapping(value = "/posts")
    public ResponseEntity<PostSliceDto> getPosts(Pageable pageable,
                                 @CookieValue(name = "Authorization") String jwt){
        String username = jwtUtil.getUsername(jwt);
        Member member = memberService.getMemberByIdentifier(username);
        PostSliceDto postsWithInfo = postFacadeService.getPostsWithInfo(pageable, member.getId());
        return ResponseEntity.ok(postsWithInfo);
    }

    //게시글 상세 조회
    @GetMapping(value = "/post/{postId}")
    public ResponseEntity<PostDetailDto> getPostDetail(@CookieValue(name = "Authorization") String jwt,
                                       @PathVariable Long postId){
        String username = jwtUtil.getUsername(jwt);
        Member member = memberService.getMemberByIdentifier(username);
        PostDetailDto postDetailWithInfo = postService.getPostDetailWithInfo(member.getId(), postId);
        return ResponseEntity.ok(postDetailWithInfo);
    }

    //회원이 작성한 글 조회
    @GetMapping(value="/user/{nickname}/posts")
    public ResponseEntity<MemberPostPageDto> getMemberPosts(Pageable pageable,
                                                              @PathVariable String nickname){
        MemberPostPageDto memberPosts = postService.getMemberPosts(pageable, nickname);
        return ResponseEntity.ok(memberPosts);
    }

    //회원이 좋아요를 누른 게시글 조회
    @GetMapping(value="/user/liked-posts")
    public ResponseEntity<CursorPageResponse> getLikedPosts(@RequestParam int pageSize,
                                                            @RequestParam(required = false) LocalDateTime cursor,
                                                            Authentication authentication){

        CustomOAuth2User principal = (CustomOAuth2User) authentication.getPrincipal();
        String identifier = principal.getUsername();
        Member member = memberService.getMemberByIdentifier(identifier);

        CursorPageResponse likedPosts = postService.getLikedPosts(pageSize, cursor, member.getId());
        return ResponseEntity.ok(likedPosts);
    }

    //회원 게시글 조회
    @GetMapping("/user/post/{postId}")
    public ResponseEntity<PostDTO> getPost(@PathVariable Long postId){

        PostDTO post = postService.getPost(postId);
        return ResponseEntity.ok(post);
    }




}
