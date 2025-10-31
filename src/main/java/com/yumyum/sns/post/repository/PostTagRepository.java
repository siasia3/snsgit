package com.yumyum.sns.post.repository;

import com.yumyum.sns.post.entity.PostTag;
import com.yumyum.sns.post.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostTagRepository extends JpaRepository<PostTag,Long> {
    List<PostTag> findByPostId(Long postId);
}
