package com.yumyum.sns.security.oauthjwt.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

    private final RedisTemplate<String, String> redisTemplate;

    // Refresh Token 저장
    public void saveRefreshToken(String username, String refreshToken, long expireTimeMs) {
        redisTemplate.opsForValue().set(
                "RT:" + username,
                refreshToken,
                expireTimeMs,
                TimeUnit.MILLISECONDS
        );
    }

    // Refresh Token 조회
    public String getRefreshToken(String username) {
        return redisTemplate.opsForValue().get("RT:" + username);
    }

    // Refresh Token 삭제
    public void deleteRefreshToken(String username) {

        try {
            redisTemplate.delete("RT:" + username);
        } catch (RedisConnectionFailureException e) {
            log.error("Redis 서버 연결 실패, Refresh Token 삭제 불가: {}", username, e);
            throw new RuntimeException("Redis 삭제 실패");
            // 필요시 사용자에게 500 에러 반환 또는 무시
        } catch (Exception e) {
            log.error("Refresh Token 삭제 중 오류 발생: {}", username, e);
            throw new RuntimeException("Redis 삭제 실패");
        }
    }
}
