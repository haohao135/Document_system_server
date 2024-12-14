package com.document.demo.repository;

import com.document.demo.models.CheckMark;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CheckMarkRepository extends MongoRepository<CheckMark, String> {
}
