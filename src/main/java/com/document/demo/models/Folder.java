package com.document.demo.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
public class Folder {
    @Id
    private String folderId;
    private String name;
    private List<String> checkMarkId;
}
