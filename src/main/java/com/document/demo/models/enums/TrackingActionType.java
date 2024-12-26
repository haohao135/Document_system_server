package com.document.demo.models.enums;

import lombok.Getter;

@Getter
public enum TrackingActionType {
    // Common actions
    CREATE("Created"),
    UPDATE("Updated"),
    DELETE("Deleted"),
    VIEW("Viewed"),
    
    // User specific
    LOGIN("Logged in"),
    LOGOUT("Logged out"),
    PASSWORD_CHANGE("Changed password"),
    PROFILE_UPDATE("Updated profile"),
    
    // Document specific
    SHARE("Shared"),
    DOWNLOAD("Downloaded"),
    PRINT("Printed"),
    STATUS_CHANGE("Changed status"),
    
    // System
    SYSTEM_CONFIG("System configuration changed"),
    MAINTENANCE("System maintenance"),
    ERROR("System error occurred");

    private final String description;

    TrackingActionType(String description) {
        this.description = description;
    }
} 