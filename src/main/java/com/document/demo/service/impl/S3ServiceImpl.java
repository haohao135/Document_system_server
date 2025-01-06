package com.document.demo.service.impl;

import com.document.demo.exception.FileUploadException;
import com.document.demo.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@Slf4j
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

    private final S3Client s3Client;
    
    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    @Override
    public String uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileUploadException("File is empty or null");
        }

        try {
            String contentType = file.getContentType();
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String folderPath = determineStoragePath(fileExtension, contentType);
            
            // Tạo folders nếu chưa tồn tại
            createFoldersIfNotExist(folderPath);
            
            String fileName = generateUniqueFileName(originalFilename);
            String fullPath = folderPath + "/" + fileName;
            
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fullPath)
                .contentType(file.getContentType())
                .build();

            s3Client.putObject(putObjectRequest, 
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            return String.format("https://%s.s3.%s.amazonaws.com/%s", 
                bucketName, region, fullPath);

        } catch (IOException e) {
            log.error("Failed to upload file to S3", e);
            throw new FileUploadException("Failed to upload file to S3: " + e.getMessage(), e);
        }
    }

    private String determineStoragePath(String fileExtension, String contentType) {
        if (contentType != null && contentType.startsWith("image/")) {
            return "images";
        }
        
        return switch (fileExtension.toLowerCase()) {
            case "pdf" -> "documents/pdf";
            case "doc", "docx" -> "documents/word";
            case "xls", "xlsx" -> "documents/excel";
            default -> "documents/others";
        };
    }

    private void createFoldersIfNotExist(String folderPath) {
        String[] folders = folderPath.split("/");
        String currentPath = "";
        
        for (String folder : folders) {
            currentPath = currentPath.isEmpty() ? folder : currentPath + "/" + folder;
            if (!folderExists(currentPath)) {
                createFolder(currentPath);
            }
        }
    }

    private boolean folderExists(String folderPath) {
        try {
            ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(folderPath + "/")
                .maxKeys(1)
                .build();
                
            ListObjectsV2Response response = s3Client.listObjectsV2(request);
            return response.hasContents();
        } catch (Exception e) {
            return false;
        }
    }

    private void createFolder(String folderPath) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(folderPath + "/")
                .build();
                
            s3Client.putObject(request, RequestBody.empty());
        } catch (Exception e) {
            log.error("Error creating folder: {}", folderPath, e);
            throw new FileUploadException("Failed to create folder: " + folderPath);
        }
    }

    private String getFileExtension(String filename) {
        return Optional.ofNullable(filename)
            .filter(f -> f.contains("."))
            .map(f -> f.substring(filename.lastIndexOf(".") + 1))
            .orElse("");
    }

    private String generateUniqueFileName(String originalFileName) {
        return UUID.randomUUID() + "_" + originalFileName;
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            String fileName = extractFileNameFromUrl(fileUrl);
            
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

            s3Client.deleteObject(deleteObjectRequest);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file from S3", e);
        }
    }

    @Override
    public byte[] downloadFile(String fileUrl) {
        try {
            String fileName = extractFileNameFromUrl(fileUrl);
            
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);
            return objectBytes.asByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to download file from S3", e);
        }
    }

    private String extractFileNameFromUrl(String fileUrl) {
        return fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
    }
} 