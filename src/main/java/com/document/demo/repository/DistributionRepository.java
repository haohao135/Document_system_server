package com.document.demo.repository;

import com.document.demo.models.Distribution;
import com.document.demo.models.Documents;
import com.document.demo.models.User;
import com.document.demo.models.enums.DistributionStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DistributionRepository extends MongoRepository<Distribution, String> {
    List<Distribution> findBySender(User sender);
    List<Distribution> findByReceiversContaining(User receiver);
    List<Distribution> findByStatus(DistributionStatus status);
    List<Distribution> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    Distribution findByDocuments(Documents documents);
} 