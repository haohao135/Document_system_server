package com.document.demo.dto.request;

import com.document.demo.models.enums.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {
    private String keyword;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private int page = 0;
    private int size = 7;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
    private DocumentType type = DocumentType.INCOMING;
} 