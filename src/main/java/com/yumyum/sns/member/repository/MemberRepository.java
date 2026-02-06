package com.yumyum.sns.member.repository;

import com.yumyum.sns.member.entity.Member;
import com.yumyum.sns.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,Long>,MemberRepositoryCustom {
    Optional<Member> findByIdentifier(String identifier);
    Optional<Member> findById(Long id);
    Optional<Member> findByNickname(String nickname);

    Optional<Member> findByUserId(String userId);
    boolean existsByUserId(String userId);
    boolean existsByNickname(String nickname);
}
