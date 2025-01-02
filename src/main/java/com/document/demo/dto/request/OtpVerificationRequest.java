package com.document.demo.dto.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class OtpVerificationRequest {
    private String email;
    private String otp;
}
