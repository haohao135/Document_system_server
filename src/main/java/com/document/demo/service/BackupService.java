package com.document.demo.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface BackupService {
    Resource backupDatabase() throws IOException;
    void restoreDatabase(MultipartFile backupFile) throws IOException;
    Resource exportCollectionToJson(String collectionName) throws IOException;
    void importCollectionFromJson(String collectionName, MultipartFile jsonFile) throws IOException;
} 