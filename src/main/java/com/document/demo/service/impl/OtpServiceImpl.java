package com.document.demo.service.impl;

import com.document.demo.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {
    private final StringRedisTemplate redisTemplate;
    private static final int OTP_EXPIRATION_MINUTES = 5;
    private static final int RESET_TOKEN_EXPIRATION_MINUTES = 30;
    private static final String RESET_TOKEN_PREFIX = "RESET_TOKEN:";
    
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
            "OTP: " + key,
            otp, 
            OTP_EXPIRATION_MINUTES, 
            TimeUnit.MINUTES
        );
    }

    @Override
    public boolean verifyOtp(String key, String otp) {
        String storedOtp = redisTemplate.opsForValue().get("OTP: " + key);
        return otp != null && otp.equals(storedOtp);
    }

    @Override
    public void deleteOtp(String key) {
        redisTemplate.delete("OTP: " + key);
    }

    @Override
    public String generateResetPasswordToken(String email) {
        String token = generateRandomToken();
        
        redisTemplate.opsForValue().set(
            RESET_TOKEN_PREFIX + email,
            token,
            RESET_TOKEN_EXPIRATION_MINUTES,
            TimeUnit.MINUTES
        );
        
        return token;
    }

    @Override
    public boolean validateResetPasswordToken(String email, String token) {
        String storedToken = redisTemplate.opsForValue().get(RESET_TOKEN_PREFIX + email);
        return token != null && token.equals(storedToken);
    }

    @Override
    public void deleteResetPasswordToken(String email) {
        redisTemplate.delete(RESET_TOKEN_PREFIX + email);
    }

    private String generateRandomToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
