package com.yumyum.sns.security.config;


import com.yumyum.sns.member.service.MemberService;
import com.yumyum.sns.security.oauthjwt.jwt.JWTFilter;
import com.yumyum.sns.security.oauthjwt.jwt.JWTUtil;
import com.yumyum.sns.security.handler.LoginSuccessHandler;
import com.yumyum.sns.security.oauthjwt.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final LoginSuccessHandler loginSuccessHandler;
    private final JWTUtil jwtUtil;
    private final MemberService memberService;

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() { // security를 적용하지 않을 리소스
        return web -> web.ignoring()
                // error endpoint를 열어줘야 함, favicon.ico 추가!
                .requestMatchers("/error","/image/**", "/css/**","/js/**", "/assets/**", "/favicon.ico");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{

        //csrf disable
        http.csrf((auth) -> auth.disable());

        //HTTP Basic 인증 방식 disable
        http.httpBasic((auth) -> auth.disable());

        //경로별 인가 작업
        http.authorizeHttpRequests((auth) -> auth
                .requestMatchers("/","/member/signup", "/resources/**","/auth/refresh"
                ,"/api/member/check-userId","/api/member/check-nickname","/api/member/signup"
                ).permitAll()
                //.requestMatchers("/main").authenticated()
                .anyRequest().authenticated());

        //Form 로그인 방식
        http.formLogin((auth) -> auth.loginPage("/").loginProcessingUrl("/process-login").successHandler(loginSuccessHandler).failureUrl("/"));

        //JWTFilter 추가
        http.addFilterBefore(new JWTFilter(jwtUtil,memberService), UsernamePasswordAuthenticationFilter.class);

        //oauth2
        http.oauth2Login((oauth2) -> oauth2
                .userInfoEndpoint((userInfoEndpointConfig) -> userInfoEndpointConfig
                    .userService(customOAuth2UserService))
                    .successHandler(loginSuccessHandler));



        //세션 설정 : STATELESS
        http.sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

}
