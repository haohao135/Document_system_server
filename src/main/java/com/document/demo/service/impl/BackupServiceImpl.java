package com.document.demo.service.impl;

import com.document.demo.service.BackupService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.types.ObjectId;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        Path exportDir = Paths.get(backupPath, collectionName);
        if (!Files.exists(exportDir)) {
            Files.createDirectories(exportDir);
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = String.format("%s_%s.json", collectionName, timestamp);
        Path exportFile = exportDir.resolve(filename);

        // Get all documents from collection
        List<Document> documents = mongoTemplate.findAll(Document.class, collectionName);
        
        // Convert to proper format with ObjectId
        List<Map<String, Object>> exportData = documents.stream()
            .map(doc -> {
                Map<String, Object> map = new HashMap<>();
                Object id = doc.get("_id");
                
                // Handle different _id formats
                if (id instanceof ObjectId) {
                    // If it's already an ObjectId, use $oid format
                    map.put("_id", new HashMap<String, String>() {{
                        put("$oid", id.toString());
                    }});
                } else if (id instanceof Document idDoc) {
                    // If it's a Document with timestamp/date, convert to ObjectId
                    if (idDoc.containsKey("timestamp")) {
                        try {
                            long timestamps = idDoc.getLong("timestamp");
                            ObjectId objectId = new ObjectId(new Date(timestamps * 1000));
                            map.put("_id", new HashMap<String, String>() {{
                                put("$oid", objectId.toString());
                            }});
                        } catch (Exception e) {
                            // If conversion fails, keep original format
                            map.put("_id", id);
                        }
                    } else {
                        map.put("_id", id);
                    }
                } else {
                    // For any other format, keep as is
                    map.put("_id", id);
                }
                
                // Copy other fields
                doc.forEach((key, value) -> {
                    if (!key.equals("_id")) {
                        map.put(key, value);
                    }
                });
                return map;
            })
            .collect(Collectors.toList());

        // Write to file with pretty printing
        objectMapper.writerWithDefaultPrettyPrinter()
                   .writeValue(exportFile.toFile(), exportData);
        
        return new UrlResource(exportFile.toUri());
    }

    @Override
    public void importCollectionFromJson(String collectionName, MultipartFile jsonFile) throws IOException {
        List<Map<String, Object>> data = objectMapper.readValue(
            jsonFile.getInputStream(),
                new TypeReference<>() {
                }
        );

        mongoTemplate.dropCollection(collectionName);

        // Convert MongoDB ObjectId format and import
        data.forEach(document -> {
            try {
                Object idObj = document.get("_id");
                if (idObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, String> idMap = (Map<String, String>) idObj;
                    if (idMap.containsKey("$oid")) {
                        document.put("_id", new ObjectId(idMap.get("$oid")));
                    } else if (idMap.containsKey("timestamp")) {
                        // Handle timestamp format
                        long timestamp = Long.parseLong(idMap.get("timestamp"));
                        document.put("_id", new ObjectId(new Date(timestamp * 1000)));
                    }
                }
                mongoTemplate.save(document, collectionName);
            } catch (Exception e) {
                log.error("Error importing document: {} - {}", document.get("_id"), e.getMessage());
                throw new RuntimeException("Error importing document", e);
            }
        });
    }
} 