package com.document.demo.repository;

import com.document.demo.models.Distribution;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DistributionRepository extends MongoRepository<Distribution, String> {
}
