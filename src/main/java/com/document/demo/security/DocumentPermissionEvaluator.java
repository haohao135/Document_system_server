package com.document.demo.security;

import com.document.demo.models.Documents;
import com.document.demo.models.User;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import com.document.demo.service.DocumentService;
import com.document.demo.service.UserService;

@Component("documentPermissionEvaluator")
@RequiredArgsConstructor
public class DocumentPermissionEvaluator {
    private final DocumentService documentService;
    private final UserService userService;

    public boolean isDocumentCreator(String documentId) {
        try {
            Documents document = documentService.findById(documentId);
            User currentUser = userService.getCurrentUser();
            return document.getCreateBy().getUserId().equals(currentUser.getUserId());
        } catch (Exception e) {
            return false;
        }
    }
} 