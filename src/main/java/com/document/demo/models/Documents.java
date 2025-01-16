package com.document.demo.models;

import com.document.demo.models.enums.DocumentStatus;
import com.document.demo.models.enums.DocumentType;
import com.document.demo.models.enums.SecretLevel;
import com.document.demo.models.enums.UrgencyLevel;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Document(collection = "documents")
public class Documents implements Cloneable{
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

    @NotBlank(message = "Agency is required")
    private String agencyUnit;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private DocumentType type = DocumentType.UNKNOWN;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    private DocumentStatus status = DocumentStatus.PENDING;

    @NotBlank(message = "Document attachment is required")
    private String attachment;
    private String keywords;
    
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private UrgencyLevel urgencyLevel = UrgencyLevel.NORMAL;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private SecretLevel secretLevel = SecretLevel.LOW;

    @Builder.Default
    private LocalDateTime expirationDate = LocalDateTime.now().plusDays(30);

    private String logNote;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @DBRef
    private User createBy;

    @Override
    public Documents clone() {
        try {
            return (Documents) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
