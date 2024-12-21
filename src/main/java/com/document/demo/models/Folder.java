package com.document.demo.models;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import lombok.*;

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
    private User createdBy; // n user - 1 folder

    @DBRef
    private List<CheckMark> checkMarks; // 1 folder - n checkMarks
}
