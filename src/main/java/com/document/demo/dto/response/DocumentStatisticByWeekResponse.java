package com.document.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentStatisticByWeekResponse {
    int week;
    int countIncomingDocument;
    int countOutgoingDocument;
}
