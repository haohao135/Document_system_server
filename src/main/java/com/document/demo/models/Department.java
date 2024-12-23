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
@Document(collection = "departments")
public class Department {
    @Id
    private String departmentId;

    @NotBlank(message = "Department name is required")
    private String name;
    private String description;

    @NotBlank(message = "Location is required")
    private String location;

    @DBRef
    private List<User> users;
}
