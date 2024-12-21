package com.document.demo.models;

import com.document.demo.models.enums.DocumentStatus;
import com.document.demo.models.enums.DocumentType;
import com.document.demo.models.enums.UrgencyLevel;
import jakarta.validation.constraints.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import java.time.LocalDateTime;
import java.util.List;

import lombok.*;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "documents")
public class Documents {
    @Id
    private String documentId;

    @NotBlank(message = "Document number is required")
    @Indexed(unique = true)
    private String number;

    @NotBlank(message = "Document title is required")
    private String title;

    @NotBlank(message = "Document content is required")
    private String content;

    @Builder.Default
    private LocalDateTime issueDate = LocalDateTime.now();

    private LocalDateTime receivedDate;
    private LocalDateTime sendDate;

    @Builder.Default
    @NotNull(message = "Document type is required")
    @Enumerated(EnumType.STRING)
    private DocumentType type = DocumentType.UNKNOWN;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    private DocumentStatus status = DocumentStatus.DRAFT;

    @NotBlank(message = "Document attachment is required")
    private String attachment;
    private String keywords;
    
    @NotNull(message = "Urgency level is required")
    @Enumerated(EnumType.STRING)
    private UrgencyLevel urgencyLevel = UrgencyLevel.NORMAL;
    
    @Builder.Default
    private LocalDateTime expirationDate = LocalDateTime.now().plusDays(30);

    private String logNote;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @DBRef
    private User createBy;

    @DBRef
    private List<CheckMark> checkMarks;

    @DBRef
    private List<Distribution> distributions;
}
