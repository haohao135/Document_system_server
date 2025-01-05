package com.document.demo.service;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

import java.io.IOException;

public interface FileStorageService {

    void init();

    String storeFile(MultipartFile file) throws IOException;

    Resource loadFileAsResource(String fileName);

    void deleteFile(String fileName);
}
