package com.document.demo.dto.response;

import com.document.demo.models.enums.DocumentStatus;
import com.document.demo.models.enums.SecretLevel;
import com.document.demo.models.enums.UrgencyLevel;
import com.fasterxml.jackson.annotation.JsonFormat;
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
public class FilterResponse {
    private List<DocumentFilterDTO> content;
    private PageableResponse pageable;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DocumentFilterDTO {
        private String documentId;
        private String number;
        private String title;
        private String agencyUnit;
        private DocumentStatus status;
        private UrgencyLevel urgencyLevel;
        private SecretLevel secretLevel;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDateTime issueDate;
        
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDateTime receivedDate;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        private LocalDateTime createdAt;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PageableResponse {
        private int number;
        private int size;
        private long totalElements;
        private int totalPages;
    }
} 