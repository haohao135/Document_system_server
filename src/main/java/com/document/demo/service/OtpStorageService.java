package com.document.demo.service;

public interface OtpStorageService {
    void save(String key, String value);
    boolean checkOtp(String key, String value);
}
