package com.document.demo.controller;

import com.document.demo.models.Tracking;
import com.document.demo.models.User;
import com.document.demo.models.enums.TrackingActionType;
import com.document.demo.models.enums.TrackingEntityType;
import com.document.demo.service.TrackingService;
import com.document.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class TrackingController {
    private final TrackingService trackingService;
    private final UserService userService;

    @GetMapping("/entity/{type}/{id}")
    public ResponseEntity<List<Tracking>> getEntityHistory(
            @PathVariable TrackingEntityType type,
            @PathVariable String id) {
        return ResponseEntity.ok(trackingService.getEntityHistory(type, id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Tracking>> getUserActivity(
            @PathVariable String userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        User user = userService.getUserById(userId);
        if (start != null && end != null) {
            return ResponseEntity.ok(trackingService.getActivityBetween(start, end));
        }
        return ResponseEntity.ok(trackingService.getUserActivity(user));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<Tracking>> getRecentActivity(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(trackingService.getRecentActivity(limit));
    }

    @GetMapping("/statistics/{entityType}")
    public ResponseEntity<Map<TrackingActionType, Long>> getActionStatistics(
            @PathVariable TrackingEntityType entityType) {
        return ResponseEntity.ok(trackingService.getActionStatistics(entityType));
    }

    @DeleteMapping("/cleanup")
    public ResponseEntity<Void> cleanupOldRecords(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime before) {
        if (before != null) {
            trackingService.deleteOldRecords(before);
        } else {
            trackingService.cleanupOldRecords();
        }
        return ResponseEntity.ok().build();
    }
} 