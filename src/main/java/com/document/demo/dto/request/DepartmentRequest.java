package com.document.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentRequest {
    @NotBlank(message = "Department name is required")
    private String name;
    
    @NotBlank(message = "Hotline is required") 
    private String hotline;
    
    private String description;
    
    @NotBlank(message = "Location is required")
    private String location;
} 