package com.document.demo.service.impl;

import com.document.demo.dto.request.DocumentRequest;
import com.document.demo.dto.request.TrackingRequest;
import com.document.demo.exception.InvalidEnumValueException;
import com.document.demo.exception.ResourceAlreadyExistsException;
import com.document.demo.exception.ResourceNotFoundException;
import com.document.demo.models.Documents;
import com.document.demo.models.User;
import com.document.demo.models.enums.DocumentStatus;
import com.document.demo.models.enums.TrackingActionType;
import com.document.demo.models.enums.TrackingEntityType;
import com.document.demo.models.enums.UrgencyLevel;
import com.document.demo.models.tracking.ChangeLog;
import com.document.demo.repository.DocumentRepository;
import com.document.demo.service.CloudinaryService;
import com.document.demo.service.DocumentService;
import com.document.demo.service.TrackingService;
import com.document.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final CloudinaryService cloudinaryService;
    private final TrackingService trackingService;
    private final UserServiceImpl userServiceImpl;

    @Override
    @Transactional
    public Documents createDocument(DocumentRequest document) throws FileUploadException {
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
                .type(document.getType())
                .urgencyLevel(document.getUrgencyLevel())
                .attachment(document.getAttachment())
                .keywords(document.getKeywords())
                .logNote(document.getLogNote())
                .createdAt(LocalDateTime.now())
                .status(DocumentStatus.DRAFT)
                .createBy(userService.getUserById(document.getUserId()))
                .build();

        if (document.getAttachment() != null && !document.getAttachment().isEmpty()) {
            String attachmentUrl = cloudinaryService.uploadFile(document.getFile());
            documents.setAttachment(attachmentUrl);
        }

        Documents savedDocument = documentRepository.save(documents);

        // Track document creation
        trackingService.track(TrackingRequest.builder()
            .actor(userServiceImpl.getCurrentUser())
            .entityType(TrackingEntityType.DOCUMENT)
            .entityId(savedDocument.getDocumentId())
            .action(TrackingActionType.CREATE)
            .metadata(Map.of(
                "number", savedDocument.getNumber(),
                "title", savedDocument.getTitle(),
                "type", savedDocument.getType(),
                "urgencyLevel", savedDocument.getUrgencyLevel().toString(),
                "status", savedDocument.getStatus().toString()
            ))
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
        if (document.getAttachment() != null && !document.getAttachment().isEmpty()) {
            changes.put("attachment", new ChangeLog());
            handleAttachmentUpdate(existingDocument, document);
        }

        Documents updatedDocument = documentRepository.save(existingDocument);
        
        // Track document update
        trackingService.track(TrackingRequest.builder()
            .actor(userServiceImpl.getCurrentUser())
            .entityType(TrackingEntityType.DOCUMENT)
            .entityId(id)
            .action(TrackingActionType.UPDATE)
            .changes(changes)
            .build());
            
        return updatedDocument;
    }

    private void handleAttachmentUpdate(Documents existingDocument, DocumentRequest document) throws FileUploadException {
        // Delete existing attachment (it maybe cannot delete if you want to restore it)
        if (existingDocument.getAttachment() != null) {
            String publicId = existingDocument.getAttachment().substring(
                existingDocument.getAttachment().lastIndexOf("/") + 1,
                existingDocument.getAttachment().lastIndexOf(".")
            );
            cloudinaryService.deleteFile(publicId);
        }
        
        String attachmentUrl = cloudinaryService.uploadFile(document.getFile());
        existingDocument.setAttachment(attachmentUrl);
    }

    @Override
    @Transactional
    public void deleteDocument(String id) {
        Documents document = findById(id);
        documentRepository.delete(document);
        
        // Track document deletion
        trackingService.track(TrackingRequest.builder()
            .actor(userServiceImpl.getCurrentUser())
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
}