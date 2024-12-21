package com.document.demo.dto.request;

import com.document.demo.models.enums.DocumentStatus;
import com.document.demo.models.enums.DocumentType;
import com.document.demo.models.enums.UrgencyLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DocumentRequest {
    @NotBlank(message = "Number is required")
    private String number;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    private LocalDateTime issueDate;
    private LocalDateTime receivedDate;
    private LocalDateTime sendDate;
    private LocalDateTime expirationDate;

    @NotNull(message = "Type is required")
    private DocumentType type;

    @NotNull(message = "Status is required")
    private DocumentStatus status;

    @NotNull(message = "Urgency level is required")
    private UrgencyLevel urgencyLevel;

    @NotBlank(message = "Attachment is required")
    private String attachment;
    private String keywords;
    private String logNote;

    @NotNull(message = "File is required")
    private MultipartFile file;

    @NotNull(message = "User is required")
    private String userId;
} 