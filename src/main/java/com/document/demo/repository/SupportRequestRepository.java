package com.document.demo.repository;

import com.document.demo.models.Support;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SupportRequestRepository extends MongoRepository<Support, String> {
}
