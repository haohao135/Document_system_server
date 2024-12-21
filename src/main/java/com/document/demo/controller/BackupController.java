package com.document.demo.controller;

import com.document.demo.service.BackupService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/backup")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class BackupController {

    private final BackupService backupService;

    @GetMapping("/export")
    public ResponseEntity<Resource> exportDatabase() throws Exception {
        Resource backup = backupService.backupDatabase();
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + backup.getFilename() + "\"")
            .contentType(MediaType.APPLICATION_JSON)
            .body(backup);
    }

    @PostMapping("/import")
    public ResponseEntity<?> importDatabase(@RequestParam("file") MultipartFile file) throws Exception {
        backupService.restoreDatabase(file);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/export/{collection}")
    public ResponseEntity<Resource> exportCollection(@PathVariable String collection) throws Exception {
        Resource backup = backupService.exportCollectionToJson(collection);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + backup.getFilename() + "\"")
            .contentType(MediaType.APPLICATION_JSON)
            .body(backup);
    }

    @PostMapping("/import/{collection}")
    public ResponseEntity<?> importCollection(
        @PathVariable String collection,
        @RequestParam("file") MultipartFile file
    ) throws Exception {
        backupService.importCollectionFromJson(collection, file);
        return ResponseEntity.ok().build();
    }
} 