package com.document.demo.service.impl;

import com.document.demo.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {
    private final StringRedisTemplate redisTemplate;
    private static final int OTP_EXPIRATION_MINUTES = 5;
    
    @Override
    public String generateOtp(int length, boolean useNumbers) {
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder(length);
        
        if (useNumbers) {
            for(int i = 0; i < length; i++) {
                otp.append(random.nextInt(10));
            }
        } else {
            String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
            for(int i = 0; i < length; i++) {
                otp.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
            }
        }
        
        return otp.toString();
    }

    @Override
    public void saveOtp(String key, String otp) {
        redisTemplate.opsForValue().set(
            "OTP:" + key, 
            otp, 
            OTP_EXPIRATION_MINUTES, 
            TimeUnit.MINUTES
        );
    }

    @Override
    public boolean verifyOtp(String key, String otp) {
        String storedOtp = redisTemplate.opsForValue().get("OTP:" + key);
        return otp != null && otp.equals(storedOtp);
    }

    @Override
    public void deleteOtp(String key) {
        redisTemplate.delete("OTP:" + key);
    }
}
