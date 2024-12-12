package com.document.demo.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
public class User {
    @Id
    private String userId;
    private String username;
    private String password;
    private String avatar;
    private String fullName;
    private String email;
    private String position;
    private String role;
    private String status;
    private List<String> folderId;
    private List<String> commentId;
    private List<String> documentId;
    private List<String> distributionId;
}
