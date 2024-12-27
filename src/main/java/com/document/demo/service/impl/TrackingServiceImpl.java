package com.document.demo.service.impl;

import com.document.demo.dto.request.TrackingRequest;
import com.document.demo.exception.ResourceNotFoundException;
import com.document.demo.models.Tracking;
import com.document.demo.models.User;
import com.document.demo.models.enums.TrackingActionType;
import com.document.demo.models.enums.TrackingEntityType;
import com.document.demo.models.tracking.DeviceInfo;
import com.document.demo.repository.TrackingRepository;
import com.document.demo.service.TrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TrackingServiceImpl implements TrackingService {
    private final TrackingRepository trackingRepository;
    private final HttpServletRequest request;

    @Override
    @Transactional
    public Tracking track(TrackingRequest request) {
        try {
            log.info("Creating tracking record for action: {} on entity: {}", 
                request.getAction(), request.getEntityType());

            DeviceInfo deviceInfo = buildDeviceInfo();
            
            Tracking tracking = Tracking.builder()
                .actor(request.getActor())
                .entityType(request.getEntityType())
                .entityId(request.getEntityId())
                .action(request.getAction())
                .description(buildDescription(request))
                .changes(request.getChanges())
                .deviceInfo(deviceInfo)
                .metadata(request.getMetadata())
                .build();
                
            return trackingRepository.save(tracking);
        } catch (Exception e) {
            log.error("Error creating tracking record: {}", e.getMessage());
            throw new ResourceNotFoundException("Failed to create tracking record");
        }
    }

    private DeviceInfo buildDeviceInfo() {
        String userAgent = request.getHeader("User-Agent");
        String ipAddress = getClientIpAddress();
        
        return DeviceInfo.builder()
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .sessionId(request.getSession().getId())
                .build();
    }

    private String getClientIpAddress() {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0];
        }
        return request.getRemoteAddr();
    }

    private String buildDescription(TrackingRequest request) {
        if (request.getDescription() != null) {
            return request.getDescription();
        }
        return String.format("%s performed %s on %s",
                request.getActor().getUsername(),
                request.getAction().toString().toLowerCase(),
                request.getEntityType().toString().toLowerCase());
    }

    @Override
    public List<Tracking> getEntityHistory(TrackingEntityType type, String entityId) {
        log.info("Fetching history for entity type: {} with ID: {}", type, entityId);
        return trackingRepository.findByEntityTypeAndEntityId(type, entityId);
    }

    @Override
    public List<Tracking> getUserActivity(User user) {
        log.info("Fetching activity for user: {}", user.getUsername());
        return trackingRepository.findByActor(user);
    }

    @Override
    public List<Tracking> getActivityBetween(LocalDateTime start, LocalDateTime end) {
        log.info("Fetching activities between {} and {}", start, end);
        return trackingRepository.findByTimestampBetween(start, end);
    }

    @Override
    public List<Tracking> getRecentActivity(int limit) {
        return trackingRepository.findAll(
            PageRequest.of(0, limit, Sort.by("timestamp").descending())
        ).getContent();
    }

    @Override
    public Map<TrackingActionType, Long> getActionStatistics(TrackingEntityType entityType) {
        List<Tracking> trackings = trackingRepository.findByEntityType(entityType);
        return trackings.stream()
                .collect(Collectors.groupingBy(
                    Tracking::getAction,
                    Collectors.counting()
                ));
    }

    @Override
    @Transactional
    public void deleteOldRecords(LocalDateTime before) {
        log.info("Deleting tracking records before: {}", before);
        trackingRepository.deleteByTimestampBefore(before);
    }

    @Override
    @Transactional
    public void cleanupOldRecords() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(90);
        deleteOldRecords(threshold);
    }
} 