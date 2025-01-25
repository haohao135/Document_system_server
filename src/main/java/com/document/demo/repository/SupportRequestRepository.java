package com.document.demo.repository;

import com.document.demo.models.SupportRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SupportRequestRepository extends MongoRepository<SupportRequest, String> {
}
