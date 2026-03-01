package com.yumyum.sns.security.oauthjwt.controller;

import com.yumyum.sns.security.common.AuthMember;
import com.yumyum.sns.security.oauthjwt.dto.CustomOAuth2User;
import com.yumyum.sns.security.oauthjwt.jwt.JWTUtil;
import com.yumyum.sns.security.oauthjwt.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class LogoutController {

    private final TokenService tokenService;
    private final JWTUtil jwtUtil;

    @PostMapping("/logout")
    public ResponseEntity logout(
            @AuthenticationPrincipal AuthMember userDetails,
            @CookieValue(value = "refreshToken", required = false) String refreshToken){

        if (refreshToken != null) {
            tokenService.deleteRefreshToken(userDetails.getIdentifier());
        }

        ResponseCookie cookie = jwtUtil.createExpiredCookie("refreshToken");

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();

    }
}
