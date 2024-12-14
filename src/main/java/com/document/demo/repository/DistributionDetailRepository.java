package com.document.demo.repository;

import com.document.demo.models.DistributionDetail;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DistributionDetailRepository extends MongoRepository<DistributionDetail, String> {
}
