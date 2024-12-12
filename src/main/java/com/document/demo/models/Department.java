package com.document.demo.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
public class Department {
    @Id
    private String departmentId;
    private String name;
    private String description;
    private String location;
    private List<String> userId;
}
