package com.document.demo.security;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Required;
import lombok.RequiredArgsConstructor;
import com.document.demo.service.DocumentService;
import com.document.demo.service.UserService;
import com.document.demo.entity.Documents;
import com.document.demo.entity.User;

@Component("documentPermissionEvaluator")
@RequiredArgsConstructor
public class DocumentPermissionEvaluator {
    private final DocumentService documentService;
    private final UserService userService;

    public boolean isDocumentCreator(String documentId) {
        try {
            Documents document = documentService.findById(documentId);
            User currentUser = userService.getCurrentUser();
            return document.getCreateBy().getId().equals(currentUser.getId());
        } catch (Exception e) {
            return false;
        }
    }
} 