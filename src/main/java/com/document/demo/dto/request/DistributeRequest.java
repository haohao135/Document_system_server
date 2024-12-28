package com.document.demo.dto.request;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class DistributeRequest {
    private String note;
    private String documentId;
    private String senderId;
    private List<String> receiverIds;
}
