package com.document.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {
    private String documentId;
    private String number;
    private String title;
    private String content;
    private LocalDateTime issueDate;
    private String type;
    private String status;
    private String urgencyLevel;
    private UserResponse creator;
    private List<DistributionResponse> distributions;
} 