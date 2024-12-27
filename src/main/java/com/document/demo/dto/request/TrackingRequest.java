package com.document.demo.dto.request;

import com.document.demo.models.User;
import com.document.demo.models.enums.TrackingActionType;
import com.document.demo.models.enums.TrackingEntityType;
import com.document.demo.models.tracking.ChangeLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackingRequest {
    @NotNull(message = "Actor is required")
    private User actor;
    
    @NotNull(message = "Entity type is required")
    private TrackingEntityType entityType;
    
    @NotNull(message = "Entity ID is required")
    private String entityId;
    
    @NotNull(message = "Action type is required")
    private TrackingActionType action;
    
    private String description;
    
    private Map<String, ChangeLog> changes;
    
    private Map<String, Object> metadata;
} 