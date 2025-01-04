package com.document.demo.models;

import com.document.demo.models.enums.CommentStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonIgnore;

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
    @JsonIgnoreProperties({"password", "comments"})
    private User user;

    @DBRef
    @JsonIgnore
    private Distribution distribution;
}
