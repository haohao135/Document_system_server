package com.document.demo.repository;

import com.document.demo.models.Documents;
import com.document.demo.models.User;
import com.document.demo.models.enums.DocumentStatus;
import com.document.demo.models.enums.DocumentType;
import com.document.demo.models.enums.UrgencyLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
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

    List<Documents> findByTypeOrderByCreatedAtDesc(DocumentType type, Pageable pageable);
    List<Documents> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Documents> findByType(DocumentType type, Pageable pageable);
    
    @Query("{ $and: [ " +
           "{ 'type': ?0 }, " +
           "{ $or: [ " +
           "{ 'number': { $regex: ?1, $options: 'i' }}, " +
           "{ 'title': { $regex: ?1, $options: 'i' }}, " +
           "{ 'content': { $regex: ?1, $options: 'i' }} " +
           "]} ]}")
    Page<Documents> findByTypeAndKeyword(DocumentType type, String keyword, Pageable pageable);

    Page<Documents> findByTypeAndStatus(DocumentType type, DocumentStatus status, Pageable pageable);
    Page<Documents> findByStatus(DocumentStatus status, Pageable pageable);
}