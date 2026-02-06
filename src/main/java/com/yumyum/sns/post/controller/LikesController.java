package com.yumyum.sns.post.controller;

import com.yumyum.sns.security.oauthjwt.jwt.JWTUtil;
import com.yumyum.sns.post.dto.LikeDto;
import com.yumyum.sns.post.service.LikesService;
import com.yumyum.sns.validated.group.DeleteGroup;
import com.yumyum.sns.validated.group.InsertGroup;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class LikesController {


    private final JWTUtil jwtUtil;
    private final LikesService likesService;


    @PostMapping(value = "/like")
    public ResponseEntity<Long> createLike(@CookieValue(name = "Authorization") String jwt,
                                        @Validated(InsertGroup.class) @RequestBody LikeDto likeDto){

        String identifier = jwtUtil.getUsername(jwt);
        Long likeId = likesService.createLike(likeDto, identifier);
        return ResponseEntity.ok(likeId);
    }

    @DeleteMapping(value = "/like")
    public ResponseEntity<Long> deleteLike(@Validated(DeleteGroup.class) @RequestBody LikeDto likeDto){
        likesService.deleteLike(likeDto.getLikeId());
        return ResponseEntity.ok(likeDto.getLikeId());
    }

}
