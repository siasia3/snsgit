package com.yumyum.sns.security.login.service;

import com.yumyum.sns.member.entity.Member;
import com.yumyum.sns.member.repository.MemberRepository;
import com.yumyum.sns.security.login.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {

        Member member = memberRepository.findByUserId(userId).orElseThrow(()->
            new UsernameNotFoundException("존재하지 않는 사용자입니다: " + userId
        ));

        return new CustomUserDetails(member);
    }
}
