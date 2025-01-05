package com.document.demo.models;

import com.document.demo.models.enums.CommentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "comments")
public class Comment {
    @Id
    private String commentId;

    @NotBlank(message = "Comment is required")
    private String comment;

    @Builder.Default
    @NotNull(message = "Timestamp is required")
    private LocalDateTime timestamp = LocalDateTime.now();

    @Builder.Default
    private CommentStatus status = CommentStatus.ORIGINAL;

    @DBRef
    private User user;

    @DBRef
    private Distribution distribution;
}
