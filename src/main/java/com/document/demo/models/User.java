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


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getFolderId() {
        return folderId;
    }

    public void setFolderId(List<String> folderId) {
        this.folderId = folderId;
    }

    public List<String> getCommentId() {
        return commentId;
    }

    public void setCommentId(List<String> commentId) {
        this.commentId = commentId;
    }

    public List<String> getDocumentId() {
        return documentId;
    }

    public void setDocumentId(List<String> documentId) {
        this.documentId = documentId;
    }

    public List<String> getDistributionId() {
        return distributionId;
    }

    public void setDistributionId(List<String> distributionId) {
        this.distributionId = distributionId;
    }
}
