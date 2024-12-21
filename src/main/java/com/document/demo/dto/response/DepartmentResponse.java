package com.document.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentResponse {
    private String id;
    private String name;
    private String description;
    private String location;
    private UserResponse manager;
    private List<UserResponse> users;
} 