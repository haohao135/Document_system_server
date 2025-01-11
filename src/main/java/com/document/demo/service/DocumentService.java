package com.document.demo.service;

import com.document.demo.dto.request.DocumentRequest;
import com.document.demo.models.Documents;
import com.document.demo.models.enums.DocumentStatus;
import com.document.demo.models.enums.DocumentType;
import com.document.demo.models.enums.UrgencyLevel;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface DocumentService {
    Documents createDocument(DocumentRequest document) throws IOException;
    Documents updateDocument(String id, DocumentRequest document) throws FileUploadException;
    void deleteDocument(String id);
    Documents findById(String id);
    Documents findByNumber(String number);
    List<Documents> findByCreator(String userId);
    List<Documents> findByStatus(String status);
    List<Documents> findByUrgencyLevel(UrgencyLevel urgencyLevel);
    List<Documents> findByDateRange(LocalDateTime start, LocalDateTime end);
    List<Documents> findAll();

    void save(Documents document);

    List<Documents> findRecentDocumentsByType(DocumentType type, int limit);
    List<Documents> findRecentDocuments(int limit);

    Page<Documents> findByType(DocumentType type, Pageable pageable);
    Page<Documents> findAll(Pageable pageable);

    Page<Documents> findByTypeAndStatus(DocumentType type, DocumentStatus status, Pageable pageable);
    Page<Documents> findByStatus(DocumentStatus status, Pageable pageable);

    Page<Documents> searchDocuments(String keyword, DocumentType type, LocalDateTime startDate,
                                    LocalDateTime endDate, Pageable pageable);

    Map<DocumentStatus, Long> getStatusCountsByType(DocumentType type);
}