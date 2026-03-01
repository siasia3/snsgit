package com.yumyum.sns.security.oauthjwt.controller;

import com.yumyum.sns.security.oauthjwt.jwt.JWTUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class RefreshController {

    private final JWTUtil jwtUtil;

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@CookieValue("refreshToken") String refreshToken, HttpServletResponse response){
        if (!jwtUtil.isExpired(refreshToken)) {
            String newAccessToken = jwtUtil.createJwt(jwtUtil.getUsername(refreshToken), "ROLE_USER", 30 * 60 * 1000L);
            response.addHeader("Set-Cookie", jwtUtil.createCookie("Authorization", newAccessToken).toString());
            return ResponseEntity.ok(Map.of("status", "ok", "message", "Access token refreshed successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", "fail", "message", "Refresh token is invalid or expired"));
        }
    }
}
