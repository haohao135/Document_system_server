package com.document.demo.service.impl;

import com.document.demo.dto.request.DocumentRequest;
import com.document.demo.dto.request.TrackingRequest;
import com.document.demo.exception.InvalidEnumValueException;
import com.document.demo.exception.ResourceAlreadyExistsException;
import com.document.demo.exception.ResourceNotFoundException;
import com.document.demo.models.Documents;
import com.document.demo.models.User;
import com.document.demo.models.enums.*;
import com.document.demo.models.tracking.ChangeLog;
import com.document.demo.repository.DocumentRepository;
import com.document.demo.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.document.demo.utils.UpdateFieldUtils.updateField;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {
    private final DocumentRepository documentRepository;
    private final UserService userService;
    private final FileStorageService fileStorageService;
    private final TrackingService trackingService;

    @Override
    @Transactional
    public Documents createDocument(DocumentRequest document) throws IOException {
        if (documentRepository.existsByNumber(document.getNumber())) {
            throw new ResourceAlreadyExistsException("Document number already exists");
        }

        Documents documents = Documents.builder()
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
                .keywords(document.getKeywords())
                .logNote(document.getLogNote())
                .createdAt(LocalDateTime.now())
                .status(document.getStatus())
                .createBy(userService.getUserById(document.getUserId()))
                .build();

        if (document.getFile() != null && !document.getFile().isEmpty()) {
            try {
                String fileName = fileStorageService.storeFile(document.getFile());
                documents.setAttachment(fileName);
            } catch (IOException e) {
                log.error("Error storing document file", e);
                throw new FileUploadException("Could not store file", e);
            }
        }

        Documents savedDocument = documentRepository.save(documents);

        // Track document creation
        trackingService.track(TrackingRequest.builder()
            .actor(userService.getCurrentUser())
            .entityType(TrackingEntityType.DOCUMENT)
            .entityId(savedDocument.getDocumentId())
            .action(TrackingActionType.CREATE)
            .metadata(Map.of(
                "document", savedDocument))
            .build());

        return savedDocument;
    }

    @Override
    @Transactional
    public Documents updateDocument(String id, DocumentRequest document) throws FileUploadException {
        Documents existingDocument = findById(id);

        if (document.getNumber() != null && !document.getNumber().equals(existingDocument.getNumber()) 
            && documentRepository.existsByNumber(document.getNumber())) {
            throw new ResourceAlreadyExistsException("Document number already exists");
        }

        Map<String, ChangeLog> changes = new HashMap<>();
        
        // Using UpdateFieldUtils pattern seen in other services
        updateField(changes, "number", existingDocument.getNumber(), document.getNumber(), existingDocument::setNumber);
        updateField(changes, "title", existingDocument.getTitle(), document.getTitle(), existingDocument::setTitle);
        updateField(changes, "content", existingDocument.getContent(), document.getContent(), existingDocument::setContent);
        updateField(changes, "issueDate", existingDocument.getIssueDate(), document.getIssueDate(), existingDocument::setIssueDate);
        updateField(changes, "receivedDate", existingDocument.getReceivedDate(), document.getReceivedDate(), existingDocument::setReceivedDate);
        updateField(changes, "sendDate", existingDocument.getSendDate(), document.getSendDate(), existingDocument::setSendDate);
        updateField(changes, "expirationDate", existingDocument.getExpirationDate(), document.getExpirationDate(), existingDocument::setExpirationDate);
        updateField(changes, "type", existingDocument.getType(), document.getType(), existingDocument::setType);
        updateField(changes, "urgencyLevel", existingDocument.getUrgencyLevel(), document.getUrgencyLevel(), existingDocument::setUrgencyLevel);
        updateField(changes, "keywords", existingDocument.getKeywords(), document.getKeywords(), existingDocument::setKeywords);
        updateField(changes, "logNote", existingDocument.getLogNote(), document.getLogNote(), existingDocument::setLogNote);
        updateField(changes, "status", existingDocument.getStatus(), document.getStatus(), existingDocument::setStatus);
        updateField(changes, "updateAt", existingDocument.getUpdatedAt(), LocalDateTime.now(), existingDocument::setUpdatedAt);

        // Handle attachment update
        if (document.getFile() != null && !document.getFile().isEmpty()) {
            changes.put("attachment", new ChangeLog());
            handleAttachmentUpdate(existingDocument, document);
        }

        Documents updatedDocument = documentRepository.save(existingDocument);
        
        // Track document update
        trackingService.track(TrackingRequest.builder()
            .actor(userService.getCurrentUser())
            .entityType(TrackingEntityType.DOCUMENT)
            .entityId(id)
            .action(TrackingActionType.UPDATE)
            .changes(changes)
            .build());
            
        return updatedDocument;
    }

    private void handleAttachmentUpdate(Documents existingDocument, DocumentRequest document) throws FileUploadException {
        try {
            // Delete existing attachment if exists
            if (existingDocument.getAttachment() != null) {
                fileStorageService.deleteFile(existingDocument.getAttachment());
            }
            
            // Store new file
            String fileName = fileStorageService.storeFile(document.getFile());
            existingDocument.setAttachment(fileName);
        } catch (IOException e) {
            log.error("Error updating document attachment", e);
            throw new FileUploadException("Could not update file", e);
        }
    }

    @Override
    @Transactional
    public void deleteDocument(String id) {
        Documents document = findById(id);
        
        // Delete attachment if exists
        if (document.getAttachment() != null) {
            try {
                fileStorageService.deleteFile(document.getAttachment());
            } catch (Exception e) {
                log.error("Error deleting document attachment", e);
            }
        }
        
        documentRepository.delete(document);
        
        // Track document deletion
        trackingService.track(TrackingRequest.builder()
            .actor(userService.getCurrentUser())
            .entityType(TrackingEntityType.DOCUMENT)
            .entityId(id)
            .action(TrackingActionType.DELETE)
            .metadata(Map.of("documentNumber", document.getNumber()))
            .build());
    }

    @Override
    public Documents findById(String id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + id));
    }

    @Override
    public Documents findByNumber(String number) {
        return documentRepository.findByNumber(number)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with number: " + number));
    }

    @Override
    public List<Documents> findByCreator(String userId) {
        User creator = userService.getUserById(userId);
        return documentRepository.findByCreateBy(creator);
    }

    @Override
    public List<Documents> findByStatus(String status) {
        try {
            DocumentStatus documentStatus = DocumentStatus.valueOf(status.toUpperCase());
            return documentRepository.findByStatus(documentStatus);
        } catch (IllegalArgumentException e) {
            throw new InvalidEnumValueException("Invalid document status: " + status);
        }
    }

    @Override
    public List<Documents> findByUrgencyLevel(UrgencyLevel urgencyLevel) {
        return documentRepository.findByUrgencyLevel(urgencyLevel);
    }

    @Override
    public List<Documents> findByDateRange(LocalDateTime start, LocalDateTime end) {
        return documentRepository.findByCreatedAtBetween(start, end);
    }

    @Override
    public List<Documents> findAll() {
        return documentRepository.findAll();
    }

    @Override
    public void save(Documents document) {
        documentRepository.save(document);
    }

    @Override
    public List<Documents> findRecentDocumentsByType(DocumentType type, int limit) {
        try {
            List<Documents> documents = documentRepository.findByTypeOrderByCreatedAtDesc(type, PageRequest.of(0, limit));
            if (documents.isEmpty()) {
                log.info("No recent documents found with type: {}", type);
            }
            return documents;
        } catch (Exception e) {
            log.error("Error finding recent documents by type: {}", type, e);
            throw new RuntimeException("Error finding recent documents by type: " + type, e);
        }
    }

    @Override
    public List<Documents> findRecentDocuments(int limit) {
        try {
            List<Documents> documents = documentRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit));
            if (documents.isEmpty()) {
                log.info("No recent documents found");
            }
            return documents;
        } catch (Exception e) {
            log.error("Error finding recent documents", e);
            throw new RuntimeException("Error finding recent documents", e);
        }
    }

    @Override
    public Page<Documents> findByType(DocumentType type, Pageable pageable) {
        try {
            Page<Documents> documents = documentRepository.findByType(type, pageable);
            if (documents.isEmpty()) {
                log.info("No documents found with type: {}", type);
            }
            return documents;
        } catch (Exception e) {
            log.error("Error finding documents by type: {}", type, e);
            throw new RuntimeException("Error finding documents by type: " + type, e);
        }
    }

    @Override
    public Page<Documents> findAll(Pageable pageable) {
        try {
            Page<Documents> documents = documentRepository.findAll(pageable);
            if (documents.isEmpty()) {
                log.info("No documents found");
            }
            return documents;
        } catch (Exception e) {
            log.error("Error finding all documents", e);
            throw new RuntimeException("Error finding all documents", e);
        }
    }

    @Override
    public Page<Documents> findByTypeAndStatus(DocumentType type, DocumentStatus status, Pageable pageable) {
        try {
            Page<Documents> documents = documentRepository.findByTypeAndStatus(type, status, pageable);
            if (documents.isEmpty()) {
                log.info("No documents found with type: {} and status: {}", type, status);
            }
            return documents;
        } catch (Exception e) {
            log.error("Error finding documents by type and status: {}, {}", type, status, e);
            throw new RuntimeException("Error finding documents by type and status", e);
        }
    }

    @Override
    public Page<Documents> findByStatus(DocumentStatus status, Pageable pageable) {
        try {
            Page<Documents> documents = documentRepository.findByStatus(status, pageable);
            if (documents.isEmpty()) {
                log.info("No documents found with status: {}", status);
            }
            return documents;
        } catch (Exception e) {
            log.error("Error finding documents by status: {}", status, e);
            throw new RuntimeException("Error finding documents by status", e);
        }
    }
}