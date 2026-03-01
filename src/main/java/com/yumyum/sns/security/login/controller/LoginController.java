package com.yumyum.sns.security.login.controller;

import com.yumyum.sns.member.entity.Member;
import com.yumyum.sns.security.login.dto.LoginRequest;
import com.yumyum.sns.security.login.service.LoginService;
import com.yumyum.sns.security.oauthjwt.jwt.JWTUtil;
import com.yumyum.sns.security.oauthjwt.service.TokenService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class LoginController {

    private final LoginService loginService;
    private final JWTUtil jwtUtil;
    private final TokenService tokenService;

    //회원 폼로그인
    @PostMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response){

        Member member = loginService.login(loginRequest);

        String accessToken = jwtUtil.createJwt(member.getIdentifier(), member.getRole(), 30*60*1000L);
        String refreshToken = jwtUtil.createRefreshToken(member.getIdentifier(),3*60*60*1000L);
        tokenService.saveRefreshToken(member.getIdentifier(),refreshToken,3*60*60*1000L);

        response.addHeader("Set-Cookie", jwtUtil.createCookie("Authorization", accessToken).toString());
        response.addHeader("Set-Cookie", jwtUtil.createCookie("refreshToken", refreshToken).toString());

        return ResponseEntity.ok().build();
    }
}
