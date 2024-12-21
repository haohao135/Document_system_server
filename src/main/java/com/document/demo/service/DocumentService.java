package com.document.demo.service;

import com.document.demo.dto.request.DocumentRequest;
import com.document.demo.models.Documents;
import com.document.demo.models.enums.UrgencyLevel;
import org.apache.tomcat.util.http.fileupload.FileUploadException;

import java.time.LocalDateTime;
import java.util.List;

public interface DocumentService {
    Documents createDocument(DocumentRequest document) throws FileUploadException;
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
}