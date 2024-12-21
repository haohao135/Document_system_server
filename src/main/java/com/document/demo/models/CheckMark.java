package com.document.demo.models;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "checkMarks")
public class CheckMark {
    @Id
    private String checkMarkId;

    @NotBlank(message = "Name is required")
    private String name;

    @DBRef
    private Folder folder;

    @DBRef
    private Documents document;
}
