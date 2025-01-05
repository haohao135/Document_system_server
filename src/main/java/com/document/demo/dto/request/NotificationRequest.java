package com.document.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequest {
    @NotBlank(message = "Message is required")
    private String message;
    
    @NotNull(message = "User ID is required")
    private String userId;
    
    @NotNull(message = "Document ID is required") 
    private String documentId;
} 