package com.document.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String userId;
    private String username;
    private String email;
    private String fullName;
    private String position;
    private String avatar;
    private String role;
    private String status;
    private DepartmentResponse department;
} 