package com.document.demo.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "folders")
public class Folder {
    @Id
    private String folderId;

    @NotBlank(message = "Folder name is required")
    private String name;

    @DBRef
    @JsonIgnore
    private User createdBy; // n user - 1 folder
}
