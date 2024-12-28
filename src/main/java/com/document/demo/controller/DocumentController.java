package com.document.demo.controller;

import com.document.demo.dto.request.DistributeRequest;
import com.document.demo.dto.request.DocumentRequest;
import com.document.demo.dto.response.DistributionResponse;
import com.document.demo.dto.response.IncomingDocumentResponse;
import com.document.demo.models.Distribution;
import com.document.demo.models.Documents;
import com.document.demo.service.CloudinaryService;
import com.document.demo.service.DistributionService;
import com.document.demo.service.DocumentService;
import com.document.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Slf4j
public class DocumentController {
    private final DocumentService documentService;
    private final DistributionService distributionService;
    private final UserService userService;
    private final CloudinaryService cloudinaryService;

    @PostMapping("/incoming/create")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<IncomingDocumentResponse> createIncomingDocument(
            @Valid @ModelAttribute DocumentRequest request) throws FileUploadException {
        
        // create document with type = INCOMING
        String attachmentUrl = cloudinaryService.uploadFile(request.getFile());
        request.setAttachment(attachmentUrl);

        Documents document = documentService.createDocument(request);

        IncomingDocumentResponse response = IncomingDocumentResponse.builder()
                .documentId(document.getDocumentId())
                .number(document.getNumber())
                .title(document.getTitle())
                .content(document.getContent())
                .issueDate(document.getIssueDate())
                .receivedDate(document.getReceivedDate())
                .sendDate(document.getSendDate())
                .expirationDate(document.getExpirationDate())
                .type(document.getType())
                .urgencyLevel(document.getUrgencyLevel())
                .attachment(document.getAttachment())
                .keywords(document.getKeywords())
                .logNote(document.getLogNote())
                .status(document.getStatus())
                .createdAt(document.getCreatedAt())
                .creator(document.getCreateBy())
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/incoming/distribute")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DistributionResponse> distributeIncomingDocument(
            @Valid @ModelAttribute DistributeRequest request) {

        Distribution distribution = distributionService.createDistribution(
                Distribution.builder()
                        .note(request.getNote())
                        .documents(documentService.findById(request.getDocumentId()))
                        .sender(userService.getUserById(request.getSenderId()))
                        .receivers(
                                request.getReceiverIds().stream()
                                .map(userService::getUserById)
                                .toList())
                        .build()
        );

        DistributionResponse response = DistributionResponse.builder()
                .distributionId(distribution.getDistributionId())
                .status(distribution.getStatus().name())
                .note(distribution.getNote())
                .timestamp(distribution.getTimestamp())
                .sender(distribution.getSender())
                .receivers(distribution.getReceivers())
                .build();

        return ResponseEntity.ok(response);
    }
} 