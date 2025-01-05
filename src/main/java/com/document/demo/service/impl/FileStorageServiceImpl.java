package com.document.demo.service.impl;

import com.document.demo.exception.FileNotFoundException;
import com.document.demo.exception.FileStorageException;
import com.document.demo.exception.FileUploadException;
import com.document.demo.service.FileStorageService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {
    private Path fileStorageLocation;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        try {
            this.fileStorageLocation = Paths.get(uploadDir)
                    .toAbsolutePath().normalize();
            Files.createDirectories(this.fileStorageLocation);
            log.info("File storage location initialized at: {}", this.fileStorageLocation);
        } catch (Exception ex) {
            log.error("Could not create upload directory: {}", uploadDir, ex);
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public String storeFile(MultipartFile file) throws FileUploadException {
        if (file == null || file.isEmpty()) {
            throw new FileUploadException("Failed to store empty file");
        }

        try {
            // Normalize file name
            String fileName = StringUtils.cleanPath(file.getOriginalFilename());

            // Check if the file's name contains invalid characters
            if(fileName.contains("..")) {
                throw new FileUploadException("Sorry! Filename contains invalid path sequence " + fileName);
            }

            // Generate unique filename
            String uniqueFileName = UUID.randomUUID() + "_" + fileName;

            // Copy file to the target location
            Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            log.info("File stored successfully: {}", uniqueFileName);
            return uniqueFileName;
        } catch (IOException ex) {
            log.error("Could not store file {}: {}", file.getOriginalFilename(), ex.getMessage());
            throw new FileUploadException("Could not store file " + file.getOriginalFilename(), ex);
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if(resource.exists()) {
                return resource;
            } else {
                log.error("File not found: {}", fileName);
                throw new FileNotFoundException("File not found: " + fileName);
            }
        } catch (MalformedURLException ex) {
            log.error("File not found: {}", fileName, ex);
            throw new FileNotFoundException("File not found: " + fileName, ex);
        }
    }

    public void deleteFile(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            boolean deleted = Files.deleteIfExists(filePath);
            
            if (deleted) {
                log.info("File deleted successfully: {}", fileName);
            } else {
                log.warn("File {} does not exist", fileName);
                throw new FileNotFoundException("File not found: " + fileName);
            }
        } catch (IOException ex) {
            log.error("Error deleting file {}: {}", fileName, ex.getMessage());
            throw new FileStorageException("Error deleting file " + fileName, ex);
        }
    }
}
