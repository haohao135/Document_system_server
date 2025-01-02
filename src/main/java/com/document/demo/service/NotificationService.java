package com.document.demo.service;

import com.document.demo.models.Notification;
import com.document.demo.models.Documents;
import java.util.List;

public interface NotificationService {
    Notification createNotification(Notification notification);
    void markAsRead(String notificationId);
    void markAllAsRead(String userId);
    void deleteNotification(String id);
    Notification findById(String id);
    List<Notification> findByUser(String userId);
    List<Notification> findUnreadByUser(String userId);
    long getUnreadCount(String userId);
    List<Notification> findByDocument(Documents document);
    List<Notification> findAll();
} 