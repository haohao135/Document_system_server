package com.document.demo.service;

public interface OtpService {
    String generateOtp(int length, boolean useNumbers);
    void saveOtp(String key, String otp);
    boolean verifyOtp(String key, String otp);
    void deleteOtp(String key);
}
