package com.document.demo.repository;

import com.document.demo.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    boolean existsByEmail(String username);
    boolean existsByUsername(String username);
    Optional<User> findByUsername(String username);
}
