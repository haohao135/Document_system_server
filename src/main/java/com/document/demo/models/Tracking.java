package com.document.demo.models;

import java.time.LocalDateTime;
import java.util.Map;

import com.document.demo.models.enums.TrackingActionType;
import com.document.demo.models.tracking.ChangeLog;
import com.document.demo.models.tracking.DeviceInfo;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import com.document.demo.models.enums.TrackingEntityType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "tracking")
public class Tracking {
    @Id
    private String trackingId;
    
    @DBRef
    private User actor;
    
    private TrackingEntityType entityType;
    
    private String entityId;
    
    @Enumerated(EnumType.STRING)
    private TrackingActionType action;
    
    private String description;
    
    private Map<String, ChangeLog> changes;
    
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    private DeviceInfo deviceInfo;

    private Map<String, Object> metadata;
}


