package com.document.demo.dto.response;

import com.document.demo.models.Department;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private String userId;
    private String username;
    private String email;
    private String fullName;
    private String position;
    private String avatar;
    private String role;
    private String status;
    private Department department;
} 