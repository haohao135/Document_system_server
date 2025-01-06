package com.document.demo.controller;

import com.document.demo.dto.request.UpdateProfileRequest;
import com.document.demo.dto.response.ErrorResponse;
import com.document.demo.dto.response.SuccessResponse;
import com.document.demo.exception.ResourceNotFoundException;
import com.document.demo.models.User;
import com.document.demo.models.enums.UserRole;
import com.document.demo.models.enums.UserStatus;
import com.document.demo.service.S3Service;
import com.document.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final S3Service s3Service;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.s3.region}") 
    private String region;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCurrentUser() {
        try {
            User currentUser = userService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Not authenticated"));
            }
            return ResponseEntity.ok(new SuccessResponse("User info retrieved successfully", currentUser));
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error retrieving user info: " + e.getMessage()));
        }
    }

    @PostMapping("/update-profile/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateProfile(@PathVariable String id,
                                              @Valid @ModelAttribute UpdateProfileRequest request) {
        try {
            User user = userService.updateProfile(id, request);
            return ResponseEntity.ok(new SuccessResponse("Background updated successfully", user));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error updating background: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserById(@PathVariable String id) {
        try {
            User user = userService.getUserById(id);
            return ResponseEntity.ok(new SuccessResponse("User retrieved successfully", user));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error retrieving user: " + e.getMessage()));
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) UserStatus status
    ) {
        try {
            Page<User> users = userService.findAll(page, size, search, role, status);
            return ResponseEntity.ok(new SuccessResponse("Users retrieved successfully", users));
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error retrieving users: " + e.getMessage()));
        }
    }

    @PostMapping("/test")
    public ResponseEntity<?> test(@RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Please select a file to upload"));
            }

            String fileUrl = s3Service.uploadFile(file);
            return ResponseEntity.ok(new SuccessResponse("File uploaded successfully", fileUrl));
            
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/test/{fileName}")
    public ResponseEntity<?> testDelete(@PathVariable String fileName) {
        try {
            String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", 
                bucketName, region, fileName);
            
            s3Service.deleteFile(fileUrl);
            return ResponseEntity.ok(new SuccessResponse("File deleted successfully", null));
            
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error deleting file: " + e.getMessage()));
        }
    }

    @GetMapping("/test/{fileName}")
    public ResponseEntity<?> testDownload(@PathVariable String fileName) {
        try {
            String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", 
                bucketName, region, fileName);
            
            byte[] data = s3Service.downloadFile(fileUrl);
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(data);
                
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error downloading file: " + e.getMessage()));
        }
    }
} 