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
    SEND_OTP("Sent OTP"),
    VERIFY_OTP("Verified OTP"),
    LOGOUT("Logged out"),
    PASSWORD_CHANGE("Changed password"),
    PROFILE_UPDATE("Updated profile"),

    // Backup
    IMPORT("Imported"),
    EXPORT("Exported"),
    
    // Document specific
    SHARE("Shared"),
    DOWNLOAD("Downloaded"),
    PRINT("Printed"),
    STATUS_CHANGE("Changed status"),

    // Department specific
    ADD_USER("Added user"),
    REMOVE_USER("Removed user"),
    
    // System
    SYSTEM_CONFIG("System configuration changed"),
    MAINTENANCE("System maintenance"),
    ERROR("System error occurred");

    private final String description;

    TrackingActionType(String description) {
        this.description = description;
    }
} 