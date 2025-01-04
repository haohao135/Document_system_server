package com.document.demo.controller;

import com.document.demo.dto.request.DistributeRequest;
import com.document.demo.dto.request.DocumentRequest;
import com.document.demo.dto.response.*;
import com.document.demo.models.Distribution;
import com.document.demo.models.Documents;
import com.document.demo.models.User;
import com.document.demo.models.enums.DocumentStatus;
import com.document.demo.models.enums.DocumentType;
import com.document.demo.service.CloudinaryService;
import com.document.demo.service.DistributionService;
import com.document.demo.service.DocumentService;
import com.document.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

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

    @GetMapping("/recent")
    public ResponseEntity<?> getRecentDocuments(
        @RequestParam(required = false) String type,
        @RequestParam(defaultValue = "7") int limit
    ) {
        try {
            List<Documents> documents;
            if (type != null) {
                DocumentType docType = DocumentType.valueOf(type.toUpperCase());
                documents = documentService.findRecentDocumentsByType(docType, limit);
            } else {
                documents = documentService.findRecentDocuments(limit);
            }

            List<IncomingDocumentResponse> responses = documents.stream()
                .map(doc -> IncomingDocumentResponse.builder()
                    .documentId(doc.getDocumentId())
                    .number(doc.getNumber())
                    .title(doc.getTitle()) 
                    .content(doc.getContent())
                    .type(doc.getType())
                    .status(doc.getStatus())
                    .urgencyLevel(doc.getUrgencyLevel())
                    .attachment(doc.getAttachment())
                    .keywords(doc.getKeywords())
                    .logNote(doc.getLogNote())
                    .issueDate(doc.getIssueDate())
                    .receivedDate(doc.getReceivedDate())
                    .sendDate(doc.getSendDate())
                    .expirationDate(doc.getExpirationDate())
                    .createdAt(doc.getCreatedAt())
                    .creator(doc.getCreateBy())
                    .build()
                ).toList();

            return ResponseEntity.ok(responses);
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("Invalid document type: " + type));
        }
    }

    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllDocuments(
            @RequestParam(required = false) DocumentType type,
            @RequestParam(required = false) DocumentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        try {
            Sort.Direction dir = Sort.Direction.fromString(direction);
            Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sortBy));
            
            Page<Documents> documents;
            if (type != null && status != null) {
                documents = documentService.findByTypeAndStatus(type, status, pageable);
            } else if (type != null) {
                documents = documentService.findByType(type, pageable);
            } else if (status != null) {
                documents = documentService.findByStatus(status, pageable);
            } else {
                documents = documentService.findAll(pageable);
            }

            Page<DocumentResponse> responses = documents.map(doc -> DocumentResponse.builder()
                    .documentId(doc.getDocumentId())
                    .number(doc.getNumber())
                    .title(doc.getTitle())
                    .content(doc.getContent())
                    .issueDate(doc.getIssueDate())
                    .receivedDate(doc.getReceivedDate())
                    .sendDate(doc.getSendDate())
                    .expirationDate(doc.getExpirationDate())
                    .type(doc.getType())
                    .status(doc.getStatus())
                    .urgencyLevel(doc.getUrgencyLevel())
                    .attachment(doc.getAttachment())
                    .keywords(doc.getKeywords())
                    .logNote(doc.getLogNote())
                    .createdAt(doc.getCreatedAt())
                    .creator(convertToUserResponse(doc.getCreateBy()))
                    .distributions(doc.getDistributions() != null ? 
                        doc.getDistributions().stream()
                            .map(this::convertToDistributionResponse)
                            .collect(Collectors.toList()) : 
                        new ArrayList<>())
                    .build()
            );

            return ResponseEntity.ok(new SuccessResponse("Documents retrieved successfully", responses));
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error retrieving documents: " + e.getMessage()));
        }
    }

    private UserResponse convertToUserResponse(User user) {
        if (user == null) return null;
        return UserResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .position(user.getPosition())
                .avatar(user.getAvatar())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .department(user.getDepartment() != null ?
                        DepartmentResponse.builder()
                            .id(user.getDepartment().getDepartmentId())
                            .name(user.getDepartment().getName())
                            .description(user.getDepartment().getDescription())
                            .location(user.getDepartment().getLocation())
                            .build() : null)
                .build();
    }

    private DistributionResponse convertToDistributionResponse(Distribution dist) {
        if (dist == null) return null;
        return DistributionResponse.builder()
                .distributionId(dist.getDistributionId())
                .status(dist.getStatus().name())
                .note(dist.getNote())
                .timestamp(dist.getTimestamp())
                .sender(dist.getSender())
                .receivers(dist.getReceivers() != null ?
                    dist.getReceivers() :
                    new ArrayList<>())
                .build();
    }
} 