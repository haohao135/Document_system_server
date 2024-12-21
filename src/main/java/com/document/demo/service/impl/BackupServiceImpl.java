package com.document.demo.service.impl;

import com.document.demo.service.BackupService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class BackupServiceImpl implements BackupService {

    private final MongoTemplate mongoTemplate;
    private final ObjectMapper objectMapper;

    @Value("${backup.path}")
    private String backupPath;

    @Override
    public Resource backupDatabase() throws IOException {
        // Create backup directory if not exists
        Path backupDir = Paths.get(backupPath);
        if (!Files.exists(backupDir)) {
            Files.createDirectories(backupDir);
        }

        // Generate backup filename with timestamp
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = String.format("backup_%s.json", timestamp);
        Path backupFile = backupDir.resolve(filename);

        // Get all collection names
        List<String> collectionNames = mongoTemplate.getCollectionNames().stream().toList();

        // Create backup object containing all collections
        var backup = collectionNames.stream()
            .collect(java.util.stream.Collectors.toMap(
                name -> name,
                name -> mongoTemplate.findAll(Object.class, name)
            ));

        // Write to file
        objectMapper.writeValue(backupFile.toFile(), backup);

        return new UrlResource(backupFile.toUri());
    }

    @Override
    public void restoreDatabase(MultipartFile backupFile) throws IOException {
        // Read backup file as TypeReference
        TypeReference<HashMap<String, List<Object>>> typeRef =
                new TypeReference<>() {
                };
        
        Map<String, List<Object>> backup = objectMapper.readValue(
            backupFile.getInputStream(), 
            typeRef
        );

        // Drop existing collections and restore from backup
        backup.forEach((collectionName, documents) -> {
            mongoTemplate.dropCollection(collectionName);
            documents.forEach(doc -> 
                mongoTemplate.save(doc, collectionName)
            );
        });
    }

    @Override
    public Resource exportCollectionToJson(String collectionName) throws IOException {
        Path exportDir = Paths.get(backupPath, "exports");
        if (!Files.exists(exportDir)) {
            Files.createDirectories(exportDir);
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = String.format("%s_%s.json", collectionName, timestamp);
        Path exportFile = exportDir.resolve(filename);

        List<?> data = mongoTemplate.findAll(Object.class, collectionName);
        objectMapper.writeValue(exportFile.toFile(), data);

        return new UrlResource(exportFile.toUri());
    }

    @Override
    public void importCollectionFromJson(String collectionName, MultipartFile jsonFile) throws IOException {
        List<?> data = objectMapper.readValue(
            jsonFile.getInputStream(),
            objectMapper.getTypeFactory().constructCollectionType(List.class, Object.class)
        );

        mongoTemplate.dropCollection(collectionName);
        data.forEach(document -> 
            mongoTemplate.save(document, collectionName)
        );
    }
} 