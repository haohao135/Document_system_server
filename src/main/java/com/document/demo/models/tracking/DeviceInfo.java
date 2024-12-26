package com.document.demo.models.tracking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceInfo {
    private String ipAddress;
    private String userAgent;
    private String deviceType;
    private String browser;
    private String operatingSystem;
    private String location;
    private String sessionId;
} 