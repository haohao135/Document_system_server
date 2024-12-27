package com.document.demo.utils;

import com.document.demo.models.Documents;
import com.document.demo.models.tracking.ChangeLog;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ChangeLogUtils {
    
    public static Map<String, ChangeLog> buildDocumentChanges(Documents oldDoc, Documents newDoc) {
        Map<String, ChangeLog> changes = new HashMap<>();
        
        // Compare status
        if (!oldDoc.getStatus().equals(newDoc.getStatus())) {
            changes.put("status", ChangeLog.builder()
                    .oldValue(oldDoc.getStatus().toString())
                    .newValue(newDoc.getStatus().toString())
                    .build());
        }
        
        // Compare urgency level
        if (!oldDoc.getUrgencyLevel().equals(newDoc.getUrgencyLevel())) {
            changes.put("urgencyLevel", ChangeLog.builder()
                    .oldValue(oldDoc.getUrgencyLevel().toString())
                    .newValue(newDoc.getUrgencyLevel().toString())
                    .build());
        }
        
        // Compare title/subject
        if (!oldDoc.getTitle().equals(newDoc.getTitle())) {
            changes.put("title", ChangeLog.builder()
                    .oldValue(oldDoc.getTitle())
                    .newValue(newDoc.getTitle())
                    .build());
        }
        
        // Compare description
        if ((oldDoc.getContent() != null && !oldDoc.getContent().equals(newDoc.getContent()))
            || (oldDoc.getContent() == null && newDoc.getContent() != null)) {
            changes.put("summary content", ChangeLog.builder()
                    .oldValue(oldDoc.getContent())
                    .newValue(newDoc.getContent())
                    .build());
        }
        
        return changes;
    }
} 