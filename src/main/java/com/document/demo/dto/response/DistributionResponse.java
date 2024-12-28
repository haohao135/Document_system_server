package com.document.demo.dto.response;

import com.document.demo.models.User;
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
public class DistributionResponse {
    private String distributionId;
    private String status;
    private String note;
    private LocalDateTime timestamp;
    private User sender;
    private List<User> receivers;
} 