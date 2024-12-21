package com.document.demo.service.impl;

import com.cloudinary.Cloudinary;
import com.document.demo.exception.FileDeleteException;
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