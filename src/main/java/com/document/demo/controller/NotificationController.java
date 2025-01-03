package com.document.demo.controller;

import com.document.demo.dto.response.ErrorResponse;
import com.document.demo.dto.response.SuccessResponse;
import com.document.demo.exception.ResourceNotFoundException;
import com.document.demo.models.Notification;
import com.document.demo.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<?> getNotifications(
            @RequestParam(required = false) String userId,
            @RequestParam(defaultValue = "false") boolean unreadOnly
    ) {
        try {
            if (userId != null) {
                List<Notification> notifications;
                if (unreadOnly) {
                    notifications = notificationService.findUnreadByUser(userId);
                } else {
                    notifications = notificationService.findByUser(userId);
                }
                
                long unreadCount = notificationService.getUnreadCount(userId);
                
                Map<String, Object> response = new HashMap<>();
                response.put("notifications", notifications);
                response.put("unreadCount", unreadCount);
                
                return ResponseEntity.ok(new SuccessResponse(
                    "Notifications retrieved successfully", 
                    response
                ));
            }
            
            // If no userId provided, return all notifications (admin only)
            return ResponseEntity.ok(new SuccessResponse(
                "All notifications retrieved successfully",
                notificationService.findAll()
            ));
            
        } catch (ResourceNotFoundException e) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error retrieving notifications: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable String id) {
        try {
            notificationService.markAsRead(id);
            return ResponseEntity.ok(new SuccessResponse("Notification marked as read"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error marking notification as read: " + e.getMessage()));
        }
    }

    @PostMapping("/mark-all-read")
    public ResponseEntity<?> markAllAsRead(@RequestParam String userId) {
        try {
            notificationService.markAllAsRead(userId);
            return ResponseEntity.ok(new SuccessResponse("All notifications marked as read"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error marking all notifications as read: " + e.getMessage()));
        }
    }
} 