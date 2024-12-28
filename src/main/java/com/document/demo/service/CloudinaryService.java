package com.document.demo.service;

import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {
    String uploadFile(MultipartFile file) throws FileUploadException;
    byte[] downloadFile(String publicId) throws FileUploadException;
    void deleteFile(String publicId);
} 