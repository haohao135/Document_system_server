package com.document.demo.dto.request;

import com.document.demo.models.enums.DocumentStatus;
import com.document.demo.models.enums.DocumentType;
import com.document.demo.models.enums.SecretLevel;
import com.document.demo.models.enums.UrgencyLevel;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilterRequest {
    private String agencyUnit;
    private DocumentStatus status;
    private UrgencyLevel urgencyLevel;
    private SecretLevel secretLevel;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime endDate;
    
    @Builder.Default
    private int page = 0;
    
    @Builder.Default
    private int size = 7;
    
    @NotNull(message = "Document type is required")
    private DocumentType type;
}