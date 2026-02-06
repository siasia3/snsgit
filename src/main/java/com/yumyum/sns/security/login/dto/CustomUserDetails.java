package com.yumyum.sns.security.login.dto;

import com.yumyum.sns.member.entity.Member;
import com.yumyum.sns.security.common.AuthMember;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;
import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails, AuthMember, Principal {

    private final Member member;

    public CustomUserDetails(Member member) {
        this.member = member;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(() -> member.getRole());
    }

    @Override
    public String getPassword() {
        return member.getPassword();
    }

    @Override
    public String getUsername() {
        return member.getIdentifier();
    }

    public Member getMember() {
        return member;
    }

    @Override
    public String getName() {
        return member.getIdentifier();
    }
    @Override
    public String getIdentifier() {
        return member.getIdentifier();
    }


}
