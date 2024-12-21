package com.document.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DistributionResponse {
    private String distributionId;
    private String status;
    private String note;
    private LocalDateTime timestamp;
    private UserResponse sender;
    private List<UserResponse> receivers;
    private List<CommentResponse> comments;
} 