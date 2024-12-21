package com.document.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private String refreshToken;
    private String id;
    private String username;
    private String email;
    private String role;
} 