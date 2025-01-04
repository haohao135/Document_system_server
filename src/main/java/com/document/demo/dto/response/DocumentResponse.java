package com.document.demo.dto.response;

import com.document.demo.models.enums.DocumentStatus;
import com.document.demo.models.enums.DocumentType;
import com.document.demo.models.enums.UrgencyLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class    DocumentResponse {
    private String documentId;
    private String number;
    private String title;
    private String content;
    private LocalDateTime issueDate;
    private LocalDateTime receivedDate;
    private LocalDateTime sendDate;
    private LocalDateTime expirationDate;
    private DocumentType type;
    private DocumentStatus status;
    private UrgencyLevel urgencyLevel;
    private String attachment;
    private String keywords;
    private String logNote;
    private LocalDateTime createdAt;
    private UserResponse creator;
    private List<DistributionResponse> distributions;
} 