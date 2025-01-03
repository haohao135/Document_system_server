package com.document.demo.service;

import com.document.demo.dto.response.EmailResponse;

public interface EmailService {
    EmailResponse sendOtpEmail(String to, String otp);
}
