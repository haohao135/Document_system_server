package com.document.demo.dto.request;

import com.document.demo.models.enums.DocumentStatus;
import com.document.demo.models.enums.DocumentType;
import com.document.demo.models.enums.SecretLevel;
import com.document.demo.models.enums.UrgencyLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateDocumentRequest {
    private String number;
    private String title; 
    private String content;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime issueDate;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime receivedDate;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime sendDate;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime expirationDate;
    
    private String agencyUnit;
    private DocumentType type;
    private DocumentStatus status;
    private UrgencyLevel urgencyLevel;
    private String keywords;
    private String logNote;
    private MultipartFile file;
    private SecretLevel secretLevel;
} 