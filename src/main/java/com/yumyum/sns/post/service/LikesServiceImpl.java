package com.yumyum.sns.post.service;

import com.yumyum.sns.error.exception.custom.BusinessException;
import com.yumyum.sns.error.exception.errorcode.ErrorCode;
import com.yumyum.sns.member.entity.Member;
import com.yumyum.sns.member.service.MemberService;
import com.yumyum.sns.post.dto.LikeDto;
import com.yumyum.sns.post.entity.Likes;
import com.yumyum.sns.post.entity.Post;
import com.yumyum.sns.post.repository.LikesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class LikesServiceImpl implements LikesService {

    private final LikesRepository likesRepository;
    private final MemberService memberService;
    private final PostService postService;

    //좋아요 추가
    @Override
    public Long createLike(LikeDto likeDto, String identifier) {
        Member checkMember = memberService.getMemberByIdentifier(identifier);
        Post post = postService.getPostById(likeDto.getPostId());

        /*if (likesRepository.existsByPostIdAndMemberId(post.getId(), checkMember.getId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_LIKE);
        }*/

        try {
            Likes savedLike = likesRepository.save(new Likes(post, checkMember));
            return savedLike.getId();
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.DUPLICATE_LIKE);
        }
    }

    //좋아요 삭제
    @Override
    public void deleteLike(Long likeId) {
        Likes like = likesRepository.findById(likeId).orElseThrow(() -> new BusinessException(ErrorCode.LIKE_NOT_FOUND));
        likesRepository.delete(like);
    }
}