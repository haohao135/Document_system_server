package com.document.demo.service.impl;

import com.document.demo.dto.request.DocumentRequest;
import com.document.demo.models.Documents;
import com.document.demo.models.User;
import com.document.demo.exception.InvalidEnumValueException;
import com.document.demo.exception.ResourceAlreadyExistsException;
import com.document.demo.exception.ResourceNotFoundException;
import com.document.demo.models.enums.DocumentStatus;
import com.document.demo.models.enums.UrgencyLevel;
import com.document.demo.repository.DocumentRepository;
import com.document.demo.service.CloudinaryService;
import com.document.demo.service.DocumentService;
import com.document.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {
    private final DocumentRepository documentRepository;
    private final UserService userService;
    private final CloudinaryService cloudinaryService;

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
            document.setAttachment(attachmentUrl);
        }

        return documentRepository.save(documents);
    }

    @Override
    @Transactional
    public Documents updateDocument(String id, DocumentRequest document) throws FileUploadException {
        Documents existingDocument = findById(id);

        if (document.getNumber() != null && !document.getNumber().equals(existingDocument.getNumber()) 
            && documentRepository.existsByNumber(document.getNumber())) {
            throw new ResourceAlreadyExistsException("Document number already exists");
        }

        if (document.getNumber() != null) {
            existingDocument.setNumber(document.getNumber());
        }
        if (document.getTitle() != null) {
            existingDocument.setTitle(document.getTitle());
        }
        if (document.getContent() != null) {
            existingDocument.setContent(document.getContent());
        }
        if (document.getIssueDate() != null) {
            existingDocument.setIssueDate(document.getIssueDate());
        }
        if (document.getReceivedDate() != null) {
            existingDocument.setReceivedDate(document.getReceivedDate());
        }
        if (document.getSendDate() != null) {
            existingDocument.setSendDate(document.getSendDate());
        }
        if (document.getExpirationDate() != null) {
            existingDocument.setExpirationDate(document.getExpirationDate());
        }
        if (document.getType() != null) {
            existingDocument.setType(document.getType());
        }
        if (document.getUrgencyLevel() != null) {
            existingDocument.setUrgencyLevel(document.getUrgencyLevel());
        }
        if (document.getKeywords() != null) {
            existingDocument.setKeywords(document.getKeywords());
        }
        if (document.getLogNote() != null) {
            existingDocument.setLogNote(document.getLogNote());
        }
        if (document.getStatus() != null) {
            existingDocument.setStatus(document.getStatus());
        }
        if (document.getAttachment() != null && !document.getAttachment().isEmpty()) {
            // Delete old attachment if exists
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

        existingDocument.setUpdatedAt(LocalDateTime.now());
        return documentRepository.save(existingDocument);
    }

    @Override
    @Transactional
    public void deleteDocument(String id) {
        Documents document = findById(id);
        documentRepository.delete(document);
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