package com.document.demo.services;

import com.document.demo.repository.DocumentsRepository;
import org.springframework.stereotype.Service;

@Service
public class DocumentsService {
    private final DocumentsRepository documentsRepository;

    public DocumentsService(DocumentsRepository documentsRepository) {
        this.documentsRepository = documentsRepository;
    }
}
