package com.document.demo.dto.response;

import com.document.demo.models.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponse {
    private String id;
    private String comment;
    private LocalDateTime timestamp;
    private User user;
    private String distributionId;
} 