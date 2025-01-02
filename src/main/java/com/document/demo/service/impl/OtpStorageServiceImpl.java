package com.document.demo.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
public class OtpStorageServiceImpl {
    private final StringRedisTemplate redisTemplate;

    // maybe use email for key and otp for value
    public void save(String key, String value) {
        redisTemplate.opsForValue().set(key, value, 5, TimeUnit.MINUTES);
    }

    public boolean checkOtp(String key, String value) {
        String redisValue = redisTemplate.opsForValue().get(key);
        return value.equals(redisValue);
    }
}
