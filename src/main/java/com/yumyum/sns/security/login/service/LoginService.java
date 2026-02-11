package com.yumyum.sns.security.login.service;

import com.yumyum.sns.error.exception.InvalidLoginException;
import com.yumyum.sns.member.entity.Member;
import com.yumyum.sns.member.repository.MemberRepository;
import com.yumyum.sns.security.login.dto.LoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    //폼 로그인 회원 확인
    public Member login(LoginRequest loginRequest){
        Member member = memberRepository.findByUserId(loginRequest.getUserId()).orElseThrow(InvalidLoginException::new);

        if(!passwordEncoder.matches(loginRequest.getPassword(), member.getPassword())){
            throw  new InvalidLoginException();
        }

        return member;
    }
}
