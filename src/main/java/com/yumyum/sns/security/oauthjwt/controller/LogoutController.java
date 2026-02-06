package com.yumyum.sns.security.oauthjwt.controller;

import com.yumyum.sns.security.common.AuthMember;
import com.yumyum.sns.security.oauthjwt.dto.CustomOAuth2User;
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

    @PostMapping("/logout")
    public ResponseEntity logout(
            @AuthenticationPrincipal AuthMember userDetails,
            @CookieValue(value = "refreshToken", required = false) String refreshToken){

        if (refreshToken != null) {
            tokenService.deleteRefreshToken(userDetails.getIdentifier());
        }

        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();

        /*AccessToken을 만료시키지 않은 이유
        1. 만료시간 자체가 짧기 때문에
        2. access토큰은 서버에 저장하지 않기 때문에 굳이 관리한다면 블랙리스트를 만들어야 하는데 그러면 서버 부하가 늘어남
        3. 어차피 로그아웃이기 때문에 남아있는 AccessToken의 위험성이 제한적
        하지만 보안 민감 서비스라면 AccessToken까지 강제 만료를 시켜야 한다. 나는 일단 refresh만 만료시키는걸로 결정했다.
         */


        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();

    }
}
