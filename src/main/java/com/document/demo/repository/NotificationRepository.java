package com.document.demo.repository;

import com.document.demo.models.Notification;
import com.document.demo.models.User;
import com.document.demo.models.Documents;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByUser(User user);
    List<Notification> findByUserAndRead(User user, boolean isRead);
    List<Notification> findByDocument(Documents document);
    List<Notification> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    long countByUserAndRead(User user, boolean isRead);
} 