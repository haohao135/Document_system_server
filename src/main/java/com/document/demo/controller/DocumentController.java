package com.document.demo.controller;

import com.document.demo.dto.request.*;
import com.document.demo.dto.response.*;
import com.document.demo.exception.ResourceNotFoundException;
import com.document.demo.models.Distribution;
import com.document.demo.models.Documents;
import com.document.demo.models.enums.DocumentStatus;
import com.document.demo.models.enums.DocumentType;
import com.document.demo.service.DistributionService;
import com.document.demo.service.DocumentService;
import com.document.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.EnumMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Slf4j
public class DocumentController {
    private final DocumentService documentService;
    private final DistributionService distributionService;
    private final UserService userService;

    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DocumentResponse> createDocument(
            @Valid @ModelAttribute DocumentRequest request) throws IOException {
        log.info("Create Document: {}", request);

        Documents document = documentService.createDocument(request);

        DocumentResponse response = DocumentResponse.builder()
                .documentId(document.getDocumentId())
                .number(document.getNumber())
                .title(document.getTitle())
                .content(document.getContent())
                .issueDate(document.getIssueDate())
                .receivedDate(document.getReceivedDate())
                .sendDate(document.getSendDate())
                .expirationDate(document.getExpirationDate())
                .agencyUnit(document.getAgencyUnit())
                .type(document.getType())
                .urgencyLevel(document.getUrgencyLevel())
                .attachment(document.getAttachment())
                .keywords(document.getKeywords())
                .logNote(document.getLogNote())
                .status(document.getStatus())
                .createdAt(document.getCreatedAt())
                .creator(document.getCreateBy())
                .secretLevel(document.getSecretLevel())
                .build();

        return ResponseEntity.ok(response);
    }
    @PostMapping("/update/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateDocument(
            @Valid @ModelAttribute UpdateDocumentRequest request,
            @PathVariable String id) {
        log.info("Update Document with id {}: {}", id, request);
        try {
            Documents document = documentService.updateDocument(id, request);
            DocumentResponse response = DocumentResponse.builder()
                    .documentId(document.getDocumentId())
                    .number(document.getNumber())
                    .title(document.getTitle())
                    .content(document.getContent())
                    .issueDate(document.getIssueDate())
                    .receivedDate(document.getReceivedDate())
                    .sendDate(document.getSendDate())
                    .expirationDate(document.getExpirationDate())
                    .agencyUnit(document.getAgencyUnit())
                    .type(document.getType())
                    .urgencyLevel(document.getUrgencyLevel())
                    .attachment(document.getAttachment())
                    .keywords(document.getKeywords())
                    .logNote(document.getLogNote())
                    .status(document.getStatus())
                    .createdAt(document.getCreatedAt())
                    .creator(document.getCreateBy())
                    .secretLevel(document.getSecretLevel())
                    .build();

            return ResponseEntity.ok(new SuccessResponse(
                "Document updated successfully",
                response
            ));
        } catch (ResourceNotFoundException e) {
            log.error("Document not found: ", e);
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("Document not found with id: " + id));
        } catch (Exception e) {
            log.error("Error updating document: ", e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error updating document: " + e.getMessage()));
        }
    }

    // TODO: Implement the distributeIncomingDocument method
    @PostMapping("/distribute")
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

            List<DocumentResponse> responses = documents.stream()
                .map(doc -> DocumentResponse.builder()
                    .documentId(doc.getDocumentId())
                    .number(doc.getNumber())
                    .title(doc.getTitle())
                    .content(doc.getContent())
                    .issueDate(doc.getIssueDate())
                    .receivedDate(doc.getReceivedDate())
                    .sendDate(doc.getSendDate())
                    .expirationDate(doc.getExpirationDate())
                    .agencyUnit(doc.getAgencyUnit())
                    .type(doc.getType())
                    .status(doc.getStatus())
                    .urgencyLevel(doc.getUrgencyLevel())
                    .attachment(doc.getAttachment())
                    .keywords(doc.getKeywords())
                    .logNote(doc.getLogNote())
                    .createdAt(doc.getCreatedAt())
                    .creator(doc.getCreateBy())
                    .secretLevel(doc.getSecretLevel())
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
                    .agencyUnit(doc.getAgencyUnit())
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
                    .creator(doc.getCreateBy())
                    .secretLevel(doc.getSecretLevel())
                    .build()
            );

            Map<DocumentStatus, Long> statusCounts = type != null ? 
                documentService.getStatusCountsByType(type) : 
                new EnumMap<>(DocumentStatus.class);

            return ResponseEntity.ok(new SuccessResponse(
                "Documents retrieved successfully", 
                PageWithStatusCountResponse.from(responses, statusCounts)
            ));
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error retrieving documents: " + e.getMessage()));
        }
    }

    @PostMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> searchDocuments(@RequestBody SearchRequest request) {
        log.info("Received search request: {}", request);

        Sort.Direction direction = Sort.Direction.fromString(request.getSortDirection());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), 
                                         Sort.by(direction, request.getSortBy()));

        try {
            Page<Documents> results = documentService.searchDocuments(
                request.getKeyword(),
                request.getType(),
                request.getStartDate(),
                request.getEndDate(),
                pageable
            );
            return ResponseEntity.ok(PageResponse.from(results));
        } catch (Exception e) {
            log.error("Error searching documents: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error searching documents: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getDocumentById(@PathVariable String id) {
        try {
            Documents document = documentService.findById(id);
            
            DocumentResponse response = DocumentResponse.builder()
                    .documentId(document.getDocumentId())
                    .number(document.getNumber())
                    .title(document.getTitle())
                    .content(document.getContent())
                    .issueDate(document.getIssueDate())
                    .receivedDate(document.getReceivedDate())
                    .sendDate(document.getSendDate())
                    .expirationDate(document.getExpirationDate())
                    .agencyUnit(document.getAgencyUnit())
                    .type(document.getType())
                    .urgencyLevel(document.getUrgencyLevel())
                    .attachment(document.getAttachment())
                    .keywords(document.getKeywords())
                    .logNote(document.getLogNote())
                    .status(document.getStatus())
                    .createdAt(document.getCreatedAt())
                    .creator(document.getCreateBy())
                    .secretLevel(document.getSecretLevel())
                    .build();

            return ResponseEntity.ok(new SuccessResponse(
                "Document retrieved successfully", 
                response
            ));
        } catch (RuntimeException e) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("Document not found with id: " + id));
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error retrieving document: " + e.getMessage()));
        }
    }

    @GetMapping("/agency-units/suggest")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> suggestAgencyUnits(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "5") int limit
    ) {
        log.info("Received agency unit suggestion request with keyword: {}", keyword);
        try {
            if (limit < 1) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Limit must be greater than 0"));
            }

            List<String> suggestions = documentService.suggestAgencyUnits(keyword, limit);
            
            return ResponseEntity.ok(new SuccessResponse(
                "Agency units suggestions retrieved successfully",
                new AgencyUnitSuggestResponse(suggestions)
            ));
        } catch (Exception e) {
            log.error("Error suggesting agency units: ", e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error suggesting agency units: " + e.getMessage()));
        }
    }

    @GetMapping("/filter")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> filterDocuments(FilterRequest request) {
        try {
            Pageable pageable = PageRequest.of(
                request.getPage(), 
                request.getSize(), 
                Sort.by(Sort.Direction.DESC, "createdAt")
            );

            Page<Documents> documents = documentService.filterDocuments(
                request,
                pageable
            );

            FilterResponse response = FilterResponse.builder()
                .content(documents.getContent().stream()
                    .map(doc -> FilterResponse.DocumentFilterDTO.builder()
                        .documentId(doc.getDocumentId())
                        .number(doc.getNumber())
                        .title(doc.getTitle())
                        .agencyUnit(doc.getAgencyUnit())
                        .status(doc.getStatus())
                        .urgencyLevel(doc.getUrgencyLevel())
                        .secretLevel(doc.getSecretLevel())
                        .issueDate(doc.getIssueDate())
                        .receivedDate(doc.getReceivedDate())
                        .createdAt(doc.getCreatedAt())
                        .build())
                    .collect(Collectors.toList()))
                .pageable(FilterResponse.PageableResponse.builder()
                    .number(documents.getNumber())
                    .size(documents.getSize())
                    .totalElements(documents.getTotalElements())
                    .totalPages(documents.getTotalPages())
                    .build())
                .build();

            return ResponseEntity.ok(new SuccessResponse(
                "Documents filtered successfully", 
                response
            ));
        } catch (Exception e) {
            log.error("Error filtering documents: ", e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error filtering documents: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or @documentPermissionEvaluator.isDocumentCreator(#id))")
    public ResponseEntity<?> deleteDocument(@PathVariable String id) {
        log.info("Delete Document with id: {}", id);
        try {
            documentService.deleteDocument(id);
            return ResponseEntity.ok(new SuccessResponse(
                "Document deleted successfully",
                null
            ));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("You don't have permission to delete this document"));
        } catch (Exception e) {
            log.error("Error deleting document: ", e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error deleting document: " + e.getMessage()));
        }
    }
} 