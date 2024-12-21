package com.document.demo.repository;

import com.document.demo.models.Department;
import com.document.demo.models.User;
import com.document.demo.models.enums.UserRole;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<User> findByDepartment(Department department);
    List<User> findByRole(UserRole role);
} 