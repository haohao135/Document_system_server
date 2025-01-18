package com.document.demo.dto.response;

import com.document.demo.models.enums.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportDetailResponse {
    private DocumentType documentType;
    private int total;
    private int unprocessed;
    private int processing;
    private int completed;
}
