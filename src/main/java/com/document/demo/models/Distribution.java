package com.document.demo.models;

import com.document.demo.models.enums.DistributionStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "distributions")
public class Distribution {
    @Id
    private String distributionId;

    @Builder.Default
    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    private DistributionStatus status = DistributionStatus.PENDING;

    private String note;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    @DBRef
    private User sender;

    @DBRef
    private List<User> receivers;

    @DBRef
    private Documents documents;

    @DBRef
    private List<Comment> comments;
}
