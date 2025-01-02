package com.document.demo.service;

import org.springframework.http.ResponseEntity;

public interface EmailService {
    ResponseEntity<?> sendEmail(String to, String subject, String content, String nameOfSystem);
}
