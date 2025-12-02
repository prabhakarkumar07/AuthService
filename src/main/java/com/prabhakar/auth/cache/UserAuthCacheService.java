package com.prabhakar.auth.cache;

import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserAuthCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final long TTL = 6; // 6 hours

    public UserAuthCacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private String buildKey(String username) {
        return "user:roles:" + username;
    }

    public void saveUserAuth(String username, CachedUserAuth data) {
        String key = buildKey(username);
        redisTemplate.opsForValue().set(key, data, TTL, TimeUnit.HOURS);
    }

    public CachedUserAuth getUserAuth(String username) {
        String key = buildKey(username);
        Object data = redisTemplate.opsForValue().get(key);
        return data != null ? (CachedUserAuth) data : null;
    }

    public void deleteUserAuth(String username) {
        String key = buildKey(username);
        redisTemplate.delete(key);
    }
}
