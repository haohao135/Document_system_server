package com.document.demo.service.impl;

import com.document.demo.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
@AllArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender javaMailSender;

    public ResponseEntity<?> sendEmail(String to, String subject, String content, String nameOfSystem) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setFrom("noreply.app.createbykien@gmail.com", nameOfSystem);
            helper.setTo(to);
            helper.setSubject(subject);

            helper.setText(content, true);

            javaMailSender.send(mimeMessage);

            return ResponseEntity.ok("Success to sent message to email " + to);
        } catch (MessagingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send email due to server error. Error: " + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Failed to send email due to invalid format. Error: " + e.getMessage());
        }
    }
}
