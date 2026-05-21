package com.yumyum.sns.post.repository;

import com.yumyum.sns.post.entity.Likes;
import com.yumyum.sns.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikesRepository extends JpaRepository<Likes,Long> {
    boolean existsByPostIdAndMemberId(Long postId, Long memberId);
}
