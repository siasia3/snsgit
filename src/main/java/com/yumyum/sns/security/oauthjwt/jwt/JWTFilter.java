package com.yumyum.sns.security.oauthjwt.jwt;

import com.yumyum.sns.member.entity.Member;
import com.yumyum.sns.member.service.MemberService;
import com.yumyum.sns.security.login.dto.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final MemberService memberService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.equals("/") || path.startsWith("/public") || path.startsWith("/auth/refresh") || path.startsWith("/member/signup")
                || path.startsWith("/api/member/signup")
                || path.startsWith("/api/member/check-userId") || path.startsWith("/api/member/check-nickname");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //인증된 사용자의 경우는 넘김
        Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();
        if (existingAuth != null && existingAuth.isAuthenticated()) {
            // 이미 인증된 사용자라면 다음 필터로 전달
            filterChain.doFilter(request, response);
            return;
        }

        
        //cookie들을 불러온 뒤 Authorization Key에 담긴 쿠키를 찾음
        String authorization = null;
        String refresh = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {

                System.out.println(cookie.getName());
                if (cookie.getName().equals("Authorization")) {

                    authorization = cookie.getValue();
                }
                if (cookie.getName().equals("refreshToken")) {
                    refresh = cookie.getValue();
                }
            }
        }

        //Authorization 헤더 검증
        if (authorization == null) {

            /*System.out.println("token null");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Access token expired");*/
            //조건이 해당되면 메소드 종료 (필수)
            filterChain.doFilter(request, response);
            return;
        }

        //토큰
        String accessToken = authorization;
        String refreshToken = refresh;
        String requestUri = request.getRequestURI();
        boolean isApiRequest = requestUri.startsWith("/api");

        //토큰 소멸 시간 검증
        if (jwtUtil.isExpired(accessToken)) {
            //api 요청인 경우
            if (isApiRequest) {
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\": \"token_expired\", \"message\": \"The token has expired. Please refresh your token.\"}");
                return;
            }
            //ssr 기반 화면요청이면서 리프레쉬 만료인 경우
            if(jwtUtil.isExpired(refreshToken)){
                response.sendRedirect("/");
            }

            //ssr 기반 화면요청이면서 리프레쉬 유효한 경우
            if(!jwtUtil.isExpired(refreshToken)){
                String newAccessToken = jwtUtil.createJwt(jwtUtil.getUsername(refreshToken), "ROLE_USER", 30 * 1000L);
                response.addCookie(jwtUtil.createCookie("Authorization", newAccessToken));
                accessToken = newAccessToken;
            }

        }
        if(!jwtUtil.isExpired(accessToken)) {
            //토큰에서 identifier과 role 획득
            String identifier = jwtUtil.getUsername(accessToken);
            String role = jwtUtil.getRole(accessToken);

            //member 조회
            Member member = memberService.getMemberByIdentifier(identifier);

            //UserDetails에 회원 정보 객체 담기
            CustomUserDetails customUser = new CustomUserDetails(member);

            //스프링 시큐리티 인증 토큰 생성
            Authentication authToken = new UsernamePasswordAuthenticationToken(customUser, null, customUser.getAuthorities());
            //세션에 사용자 등록
            SecurityContextHolder.getContext().setAuthentication(authToken);
            CustomUserDetails user = (CustomUserDetails) authToken.getPrincipal();

        }

        filterChain.doFilter(request, response);
    }
}
