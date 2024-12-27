package com.document.demo.service;

import com.document.demo.dto.request.TrackingRequest;
import com.document.demo.models.Tracking;
import com.document.demo.models.User;
import com.document.demo.models.enums.TrackingActionType;
import com.document.demo.models.enums.TrackingEntityType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface TrackingService {
    Tracking track(TrackingRequest request);

    List<Tracking> getEntityHistory(TrackingEntityType type, String entityId);
    List<Tracking> getUserActivity(User user);
    List<Tracking> getActivityBetween(LocalDateTime start, LocalDateTime end);
    List<Tracking> getRecentActivity(int limit);

    Map<TrackingActionType, Long> getActionStatistics(TrackingEntityType entityType);

    void deleteOldRecords(LocalDateTime before);
    void cleanupOldRecords();
} 