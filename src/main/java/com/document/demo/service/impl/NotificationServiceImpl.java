package com.document.demo.service.impl;

import com.document.demo.dto.request.NotificationRequest;
import com.document.demo.dto.request.TrackingRequest;
import com.document.demo.exception.ResourceNotFoundException;
import com.document.demo.models.Notification;
import com.document.demo.models.Documents;
import com.document.demo.models.User;
import com.document.demo.models.enums.TrackingActionType;
import com.document.demo.models.enums.TrackingEntityType;
import com.document.demo.repository.NotificationRepository;
import com.document.demo.service.DocumentService;
import com.document.demo.service.NotificationService;
import com.document.demo.service.UserService;
import com.document.demo.service.TrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserService userService;
    private final TrackingService trackingService;
    private final DocumentService documentService;

    @Override
    @Transactional
    public Notification createNotification(NotificationRequest notification) {
        User user = userService.getUserById(notification.getUserId());
        Documents document = documentService.findById(notification.getDocumentId());

        Notification savedNotification = notificationRepository.save(Notification.builder()
            .message(notification.getMessage())
            .user(user)
            .document(document)
            .build());
        
        // Track notification creation
        trackingService.track(TrackingRequest.builder()
            .actor(userService.getCurrentUser())
            .entityType(TrackingEntityType.NOTIFICATION)
            .entityId(savedNotification.getNotificationId())
            .action(TrackingActionType.CREATE)
            .metadata(Map.of(
                "userId", savedNotification.getUser().getUserId(),
                "documentId", savedNotification.getDocument().getDocumentId(),
                    "timestamp", LocalDateTime.now().toString()
            ))
            .build());
            
        return savedNotification;
    }

    @Override
    @Transactional
    public void markAsRead(String notificationId) {
        Notification notification = findById(notificationId);
        notification.setRead(true);
        notificationRepository.save(notification);
        
        trackingService.track(TrackingRequest.builder()
            .actor(userService.getCurrentUser())
            .entityType(TrackingEntityType.NOTIFICATION)
            .entityId(notificationId)
            .action(TrackingActionType.UPDATE)
            .metadata(Map.of("status", "READ"))
            .build());
    }

    @Override
    @Transactional
    public void markAllAsRead(String userId) {
        User user = userService.getUserById(userId);
        List<Notification> unreadNotifications = notificationRepository.findByUserAndRead(user, false);
        unreadNotifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }

    @Override
    public void deleteNotification(String id) {
        Notification notification = findById(id);
        notificationRepository.delete(notification);
    }

    @Override
    public Notification findById(String id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
    }

    @Override
    public List<Notification> findByUser(String userId) {
        User user = userService.getUserById(userId);
        return notificationRepository.findByUserOrderByTimestampDesc(user);
    }

    @Override
    public List<Notification> findUnreadByUser(String userId) {
        User user = userService.getUserById(userId);
        return notificationRepository.findByUserAndReadOrderByTimestampDesc(user, false);
    }

    @Override
    public long getUnreadCount(String userId) {
        User user = userService.getUserById(userId);
        return notificationRepository.countByUserAndRead(user, false);
    }

    @Override
    public List<Notification> findByDocument(Documents document) {
        return notificationRepository.findByDocument(document);
    }

    @Override
    public List<Notification> findAll() {
        return notificationRepository.findAll();
    }
} 