package com.document.demo.dto.response;

import com.document.demo.models.User;
import com.document.demo.models.enums.DocumentStatus;
import com.document.demo.models.enums.DocumentType;
import com.document.demo.models.enums.UrgencyLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncomingDocumentResponse {
    private String documentId;
    private String number;
    private String title;
    private String content;
    private DocumentType type;
    private DocumentStatus status;
    private UrgencyLevel urgencyLevel;
    private String attachment;
    private String keywords;
    private String logNote;
    private LocalDateTime issueDate;
    private LocalDateTime receivedDate;
    private LocalDateTime sendDate;
    private LocalDateTime expirationDate;
    private LocalDateTime createdAt;
    private User creator;
}
