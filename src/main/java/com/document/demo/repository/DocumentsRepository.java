package com.document.demo.repository;

import com.document.demo.models.Documents;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DocumentsRepository extends MongoRepository<Documents, String> {
}
