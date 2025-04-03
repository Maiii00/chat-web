package com.example.chat.service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class LogoutService {
    private final RedisTemplate<String, String> redisTemplate;

    // 使 JWT 失效（加入黑名單）
    public void invalidateToken(String token, long expirationMillis) {
        String key = "blacklist:" + token;
        redisTemplate.opsForValue().set("blacklist:" + token, "invalid", Duration.ofMillis(expirationMillis));
        redisTemplate.expire(key, expirationMillis, TimeUnit.MILLISECONDS);
    }

    // 檢查 JWT 是否無效
    public boolean isTokenInvalid(String token) {
        return redisTemplate.hasKey("blacklist:" + token);
    }
}
