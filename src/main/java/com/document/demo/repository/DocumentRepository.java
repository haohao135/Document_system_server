package com.document.demo.repository;

import com.document.demo.models.Documents;
import com.document.demo.models.User;
import com.document.demo.models.enums.DocumentStatus;
import com.document.demo.models.enums.UrgencyLevel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends MongoRepository<Documents, String> {
    Optional<Documents> findByNumber(String number);
    List<Documents> findByCreateBy(User creator);
    List<Documents> findByStatus(DocumentStatus status);
    List<Documents> findByUrgencyLevel(UrgencyLevel urgencyLevel);
    List<Documents> findByIssueDateBetween(LocalDateTime start, LocalDateTime end);

    boolean existsByNumber(String number);

    List<Documents> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}