package com.yumyum.sns.security.handler;


import com.yumyum.sns.security.common.AuthMember;
import com.yumyum.sns.security.oauthjwt.jwt.JWTUtil;
import com.yumyum.sns.security.oauthjwt.service.TokenService;
import jakarta.servlet.ServletException;
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
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;
    private final TokenService tokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        //OAuth2User or UserDetails
        AuthMember customUserDetails = (AuthMember) authentication.getPrincipal();

        String username = customUserDetails.getIdentifier();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        String accessToken = jwtUtil.createJwt(username, role, 30*60*1000L);
        String refreshToken = jwtUtil.createRefreshToken(username,3*60*60*1000L);
        tokenService.saveRefreshToken(username,refreshToken,3*60*60*1000L);

        response.addHeader("Set-Cookie", jwtUtil.createCookie("Authorization", accessToken).toString());
        response.addHeader("Set-Cookie", jwtUtil.createCookie("refreshToken", refreshToken).toString());
        response.sendRedirect("/main");
    }


}
