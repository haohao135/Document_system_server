package com.document.demo.service.impl;

import com.document.demo.dto.response.EmailResponse;
import com.document.demo.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;
    private static final String FROM_EMAIL = "noreply.app.createbykien@gmail.com";
    private static final String SYSTEM_NAME = "Document Management System";

    @Override
    public EmailResponse sendOtpEmail(String to, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
            
            helper.setFrom(FROM_EMAIL, SYSTEM_NAME);
            helper.setTo(to);
            helper.setSubject("Your OTP Code");
            
            String content = String.format(
                "Your OTP code is: %s\nThis code will expire in 5 minutes.",
                otp
            );
            
            helper.setText(content);
            mailSender.send(message);
            
            log.info("OTP email sent successfully to: {}", to);
            return EmailResponse.builder()
                .success(true)
                .message("OTP email sent successfully")
                .build();
                
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Failed to send OTP email to {}: {}", to, e.getMessage());
            return EmailResponse.builder()
                .success(false)
                .message("Failed to send OTP email: " + e.getMessage())
                .build();
        }
    }
}
