package com.document.demo.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentStatisticByMonthResponse {
    int month;
    int countIncomingDocument;
    int countOutgoingDocument;
}
