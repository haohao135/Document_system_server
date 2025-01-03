package com.document.demo.controller;

import com.document.demo.dto.request.TrackingRequest;
import com.document.demo.dto.response.ErrorResponse;
import com.document.demo.dto.response.SuccessResponse;
import com.document.demo.models.User;
import com.document.demo.models.enums.TrackingActionType;
import com.document.demo.models.enums.TrackingEntityType;
import com.document.demo.service.BackupService;
import com.document.demo.service.TrackingService;
import com.document.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;

@Slf4j
@RestController
@RequestMapping("/api/backup")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class BackupController {

    private final BackupService backupService;
    private final TrackingService trackingService;
    private final UserService userService;

    @GetMapping("/export")
    public ResponseEntity<?> exportDatabase(Authentication authentication) {
        try {
            Resource backup = backupService.backupDatabase();
            
            // Track export action
            if (authentication != null) {
                String username = authentication.getName();
                User user = userService.findByUsername(username);
                
                trackingService.track(
                    TrackingRequest.builder()
                        .actor(user)
                        .entityType(TrackingEntityType.DATABASE)
                        .action(TrackingActionType.EXPORT)
                        .description("Full database export")
                        .build()
                );
            }

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + backup.getFilename() + "\"")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Export-Status", "Success")
                .header("X-Export-Message", "Database exported successfully to " + backup.getFilename())
                .body(backup);

        } catch (Exception e) {
            log.error("Failed to export database", e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to export database: " + e.getMessage()));
        }
    }

    @PostMapping("/import")
    public ResponseEntity<?> importDatabase(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Please select a file to import"));
            }

            backupService.restoreDatabase(file);

            // Track import action
            if (authentication != null) {
                String username = authentication.getName();
                User user = userService.findByUsername(username);
                
                trackingService.track(
                    TrackingRequest.builder()
                        .actor(user)
                        .entityType(TrackingEntityType.DATABASE)
                        .action(TrackingActionType.IMPORT)
                        .description("Full database import")
                        .metadata(Map.of(
                            "filename", file.getOriginalFilename(),
                            "size", file.getSize(),
                            "contentType", file.getContentType()
                        ))
                        .build()
                );
            }

            return ResponseEntity.ok(new SuccessResponse(
                "Database imported successfully", 
                Map.of(
                    "filename", file.getOriginalFilename(),
                    "size", file.getSize(),
                    "timestamp", LocalDateTime.now()
                )
            ));

        } catch (Exception e) {
            log.error("Failed to import database", e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to import database: " + e.getMessage()));
        }
    }

    @GetMapping("/export/{collection}")
    public ResponseEntity<?> exportCollection(
            @PathVariable String collection,
            Authentication authentication) {
        try {
            Resource backup = backupService.exportCollectionToJson(collection);
            
            // Track collection export
            if (authentication != null) {
                String username = authentication.getName();
                User user = userService.findByUsername(username);
                
                trackingService.track(
                    TrackingRequest.builder()
                        .actor(user)
                        .entityType(TrackingEntityType.DATABASE)
                        .action(TrackingActionType.EXPORT)
                        .description("Collection export: " + collection)
                        .metadata(Map.of(
                            "collection", collection,
                            "filename", backup.getFilename()
                        ))
                        .build()
                );
            }

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                       "attachment; filename=\"" + backup.getFilename() + "\"")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Export-Status", "Success")
                .header("X-Export-Message", 
                       "Collection '" + collection + "' exported successfully to " + backup.getFilename())
                .body(backup);

        } catch (IllegalArgumentException e) {
            // Collection không tồn tại
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("Collection not found: " + collection));
        } catch (Exception e) {
            log.error("Failed to export collection: " + collection, e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to export collection: " + e.getMessage()));
        }
    }

    @PostMapping("/import/{collection}")
    public ResponseEntity<?> importCollection(
            @PathVariable String collection,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Please select a file to import"));
            }

            // Validate file type
            if (!file.getContentType().equals(MediaType.APPLICATION_JSON_VALUE)) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Only JSON files are allowed"));
            }

            backupService.importCollectionFromJson(collection, file);

            // Track collection import
            if (authentication != null) {
                String username = authentication.getName();
                User user = userService.findByUsername(username);
                
                trackingService.track(
                    TrackingRequest.builder()
                        .actor(user)
                        .entityType(TrackingEntityType.DATABASE)
                        .action(TrackingActionType.IMPORT)
                        .description("Collection import: " + collection)
                        .metadata(Map.of(
                            "collection", collection,
                            "filename", file.getOriginalFilename(),
                            "size", file.getSize(),
                            "contentType", file.getContentType()
                        ))
                        .build()
                );
            }

            return ResponseEntity.ok(new SuccessResponse(
                "Collection '" + collection + "' imported successfully",
                Map.of(
                    "collection", collection,
                    "filename", file.getOriginalFilename(),
                    "size", file.getSize(),
                    "timestamp", LocalDateTime.now()
                )
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("Invalid collection: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to import collection: " + collection, e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to import collection: " + e.getMessage()));
        }
    }
} 