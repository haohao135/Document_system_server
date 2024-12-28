package com.document.demo.service.impl;

import com.cloudinary.Cloudinary;
import com.document.demo.exception.FileDeleteException;
import com.document.demo.exception.FileDownloadException;
import com.document.demo.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {
    private final Cloudinary cloudinary;

    @Override
    public String uploadFile(MultipartFile file) {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("resource_type", "auto");
            
            Map<?, ?> uploadResult = cloudinary.uploader()
                    .upload(file.getBytes(), params);
            
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            log.error("Error uploading file to Cloudinary", e);
            throw new FileDeleteException("Could not upload file: " + e.getMessage());
        }
    }

    @Override
    public byte[] downloadFile(String fileUrl) {
        try {
            // Extract public ID from URL
            String publicId = extractPublicIdFromUrl(fileUrl);
            log.info("Downloading file from Cloudinary with public ID: {}", publicId);
            
            // Download using URL instead of public ID
            java.net.URL url = new java.net.URL(fileUrl);
            try (java.io.InputStream inputStream = url.openStream();
                 java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream()) {
                
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                
                return outputStream.toByteArray();
            }
        } catch (IOException e) {
            log.error("Error downloading file from Cloudinary URL: {}", fileUrl, e);
            throw new FileDownloadException("Could not download file: " + e.getMessage());
        }
    }

    private String extractPublicIdFromUrl(String fileUrl) {
        try {
            String[] urlParts = fileUrl.split("/");
            String fileName = urlParts[urlParts.length - 1];
            return fileName.substring(0, fileName.lastIndexOf('.'));
        } catch (Exception e) {
            log.error("Error extracting public ID from URL: {}", fileUrl, e);
            throw new IllegalArgumentException("Invalid Cloudinary URL format");
        }
    }

    @Override
    public void deleteFile(String publicId) {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("resource_type", "auto");
            
            cloudinary.uploader().destroy(publicId, params);
        } catch (IOException e) {
            log.error("Error deleting file from Cloudinary", e);
            throw new FileDeleteException("Could not delete file: " + e.getMessage());
        }
    }
} 