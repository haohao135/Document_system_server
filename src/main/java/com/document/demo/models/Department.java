package com.document.demo.models;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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

    @NotBlank(message = "Hotline is required")
    private String hotline;
    private String description;

    @NotBlank(message = "Location is required")
    private String location;
}
