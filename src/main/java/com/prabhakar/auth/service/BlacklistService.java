package com.prabhakar.auth.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class BlacklistService {

    private final StringRedisTemplate redis;

    public BlacklistService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    // expirySeconds must be > 0
    public void blacklistToken(String token, long expirySeconds) {
        if (token == null || expirySeconds <= 0) return;
        redis.opsForValue().set("blacklist:" + token, "true", expirySeconds, TimeUnit.SECONDS);
    }

    public boolean isBlacklisted(String token) {
        if (token == null) return false;
        return Boolean.TRUE.equals(redis.hasKey("blacklist:" + token));
    }
}
