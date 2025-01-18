package com.document.demo.repository;

import com.document.demo.models.Documents;
import com.document.demo.models.User;
import com.document.demo.models.enums.DocumentStatus;
import com.document.demo.models.enums.DocumentType;
import com.document.demo.models.enums.SecretLevel;
import com.document.demo.models.enums.UrgencyLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
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

    @Aggregation(pipeline = {
        // Join với collection users
        "{ $lookup: { " +
            "from: 'users', " +
            "localField: 'createBy.$id', " +
            "foreignField: '_id', " +
            "as: 'creator' " +
        "} }",
        // Unwind để chuyển array creator thành object
        "{ $unwind: { " +
            "path: '$creator', " +
            "preserveNullAndEmptyArrays: true " +
        "} }",
        // Match với điều kiện tìm kiếm
        "{ $match: { " +
            "$and: [ " +
                // Type condition (if provided)
                "{ $or: [ " +
                    "{ 'type': ?3 }, " +  // Match exact type if provided
                    "{ $expr: { $eq: [?3, null] } }" +  // Skip type condition if null
                "] }, " +
                // Date range condition (if provided)
                "{ $or: [ " +
                    "{ 'issueDate': { $gte: ?1, $lte: ?2 }}, " +
                    "{ 'receivedDate': { $gte: ?1, $lte: ?2 }}, " +
                    "{ 'sendDate': { $gte: ?1, $lte: ?2 }}, " +
                    "{ 'createdAt': { $gte: ?1, $lte: ?2 }} " +
                "] }, " +
                // Keyword search
                "{ $or: [ " +
                    // Document fields
                    "{ 'number': { $regex: ?0, $options: 'i' }}, " +
                    "{ 'title': { $regex: ?0, $options: 'i' }}, " +
                    "{ 'content': { $regex: ?0, $options: 'i' }}, " +
                    "{ 'agencyUnit': { $regex: ?0, $options: 'i' }}, " +
                    "{ 'keywords': { $regex: ?0, $options: 'i' }}, " +
                    "{ 'logNote': { $regex: ?0, $options: 'i' }}, " +
                    "{ 'type': { $regex: ?0, $options: 'i' }}, " +
                    "{ 'status': { $regex: ?0, $options: 'i' }}, " +
                    "{ 'urgencyLevel': { $regex: ?0, $options: 'i' }}, " +
                    "{ 'secretLevel': { $regex: ?0, $options: 'i' }}, " +
                    
                    // User fields
                    "{ 'creator.username': { $regex: ?0, $options: 'i' }}, " +
                    "{ 'creator.fullName': { $regex: ?0, $options: 'i' }}, " +
                    "{ 'creator.email': { $regex: ?0, $options: 'i' }} " +
                "] } " +
            "] " +
        "} }",
        // Sort
        "{ $sort: { 'createdAt': -1 } }"
    })
    List<Documents> searchDocumentsWithCreator(String keyword, LocalDateTime startDate, LocalDateTime endDate, DocumentType type);

    @Query(value = "{ 'type': ?0 }", count = true)
    long countByType(DocumentType type);

    @Query(value = "{ 'type': ?0, 'status': ?1 }", count = true)
    long countByTypeAndStatus(DocumentType type, DocumentStatus status);

    @Aggregation(pipeline = {
        "{ $match: { 'agencyUnit': { $regex: ?0, $options: 'i' } } }",
        "{ $group: { _id: '$agencyUnit' } }",
        "{ $limit: ?1 }"
    })
    List<String> suggestAgencyUnits(String keyword, int limit);

    @Query("{ $and: [ " +
           "{ 'type': ?0 }, " +
           "{ 'agencyUnit': { $regex: ?1, $options: 'i' } }, " +
           "{ $or: [ " +
               "{ 'status': ?2 }, " +
               "{ $expr: { $eq: [?2, null] } }" +
           "] }, " +
           "{ $or: [ " +
               "{ 'urgencyLevel': ?3 }, " +
               "{ $expr: { $eq: [?3, null] } }" +
           "] }, " +
           "{ $or: [ " +
               "{ 'secretLevel': ?4 }, " +
               "{ $expr: { $eq: [?4, null] } }" +
           "] }, " +
           "{ $or: [ " +
               "{ 'receivedDate': { $gte: ?5, $lte: ?6 } }, " +
               "{ $and: [ " +
                   "{ $expr: { $eq: [?5, null] } }, " +
                   "{ $expr: { $eq: [?6, null] } } " +
               "] }" +
           "] }" +
           "] }")
    Page<Documents> filterDocuments(
        DocumentType type,
        String agencyUnit,
        DocumentStatus status,
        UrgencyLevel urgencyLevel,
        SecretLevel secretLevel,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Pageable pageable
    );
    @Query("{'type': ?0, 'createdAt': { $gte: ?1, $lt: ?2 }}")
    long countByTypeAndDateRange(DocumentType type, LocalDateTime startDate, LocalDateTime endDate);

    @Query("{'createdAt': { $gte: ?0, $lt: ?1 }}")
    long countByDateRange(LocalDateTime startDate, LocalDateTime endDate);
}