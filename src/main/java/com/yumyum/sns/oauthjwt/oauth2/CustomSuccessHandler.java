package com.yumyum.sns.oauthjwt.oauth2;


import com.yumyum.sns.oauthjwt.dto.CustomOAuth2User;
import com.yumyum.sns.oauthjwt.jwt.JWTUtil;
import com.yumyum.sns.oauthjwt.service.TokenService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

@RequiredArgsConstructor
@Component
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;
    private final TokenService tokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        //OAuth2User
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();

        String username = customUserDetails.getUsername();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        String accessToken = jwtUtil.createJwt(username, role, 30*1000L);
        String refreshToken = jwtUtil.createRefreshToken(username,3*60*60*1000L);
        tokenService.saveRefreshToken(username,refreshToken,3*60*60*1000L);

        response.addCookie(jwtUtil.createCookie("Authorization", accessToken));
        response.addCookie(jwtUtil.createCookie("refreshToken", refreshToken));
        response.sendRedirect("/main");
    }


}
