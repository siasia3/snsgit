package com.yumyum.sns.security.oauthjwt.dto;

import com.yumyum.sns.security.common.AuthMember;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;


public class CustomOAuth2User implements OAuth2User, AuthMember {

    private final UserDTO userDTO;
    private final Map<String, Object> attributes;

    public CustomOAuth2User(UserDTO userDTO) {
        this.userDTO = userDTO;
        this.attributes = new HashMap<>();
        this.attributes.put("name", userDTO.getName());
    }

    @Override
    public Map<String, Object> getAttributes() {

        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collection = new ArrayList<>();

        collection.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return userDTO.getRole();
            }
        });

        return collection;
    }

    @Override
    public String getName() {
        return userDTO.getUsername();
    }

    public String getUserName() {
        return userDTO.getUsername();
    }

    @Override
    public String getIdentifier() {
        return userDTO.getUsername();
    }
}
