package com.prabhakar.auth.otp;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class OtpService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final long OTP_TTL_MINUTES = 5;

    public OtpService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private String buildKey(String email) {
        return "otp:" + email;
    }

    public void saveOtp(String email, String otp) {
        redisTemplate.opsForValue()
                .set(buildKey(email), otp, OTP_TTL_MINUTES, TimeUnit.MINUTES);
    }

    public String getOtp(String email) {
        Object value = redisTemplate.opsForValue().get(buildKey(email));
        return value != null ? value.toString() : null;
    }

    public void deleteOtp(String email) {
        redisTemplate.delete(buildKey(email));
    }
}
