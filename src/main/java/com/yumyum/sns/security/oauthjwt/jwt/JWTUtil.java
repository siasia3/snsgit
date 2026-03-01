package com.yumyum.sns.security.oauthjwt.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;

import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JWTUtil {

    @Value("${cookie.secure}")
    private boolean secure;

    @Value("${cookie.samesite}")
    private String sameSite;

    private SecretKey secretKey;

    public JWTUtil(@Value("${spring.jwt.secret}") String secret) {


        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    public String getUsername(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("identifier", String.class);
    }

    public String getRole(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
    }

    public Boolean isExpired(String token) {
        try{
            Date expiration = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration();
            return expiration.before(new Date());
        }catch (ExpiredJwtException e){
            return true;
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false; // 토큰이 유효하지 않으면 false
        }
    }



    public String createJwt(String username, String role, Long expiredMs) {

        return Jwts.builder()
                .claim("identifier", username)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }

    public String createRefreshToken(String username, Long expiredMs) {
        return Jwts.builder()
                .claim("identifier", username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }

    //나중에 쿠키 시간 파라미터 추가
    public ResponseCookie createCookie(String key, String value) {
        return ResponseCookie.from(key, value)
                .maxAge(24*60*60)
                .secure(secure)
                .path("/")
                .httpOnly(true)
                .sameSite(sameSite)
                .build();
    }

    //로그아웃 시 만료된 쿠키 생성
    public ResponseCookie createExpiredCookie(String key) {
        return ResponseCookie.from(key, "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .secure(secure)
                .sameSite(sameSite)
                .build();
    }
}