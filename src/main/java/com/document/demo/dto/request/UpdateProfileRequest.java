package com.document.demo.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {
    private String userName;
    private String fullName;
    private String position;
    private String email;
    private String phone;
    private String avatar;
    private String background;
    private MultipartFile avatarFile;
    private MultipartFile backgroundFile;
} 