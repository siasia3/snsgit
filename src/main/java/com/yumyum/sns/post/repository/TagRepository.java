package com.yumyum.sns.post.repository;

import com.yumyum.sns.member.entity.Member;
import com.yumyum.sns.post.entity.Post;
import com.yumyum.sns.post.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag,Long> {
    Optional<Tag> findByContent(String content);
}
