package com.document.demo.repository;

import com.document.demo.models.Department;
import com.document.demo.models.User;
import com.document.demo.models.enums.UserRole;
import com.document.demo.models.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
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
    
    @Query("{ $or: [ " +
           "{ 'username': { $regex: ?0, $options: 'i' }}, " +
           "{ 'email': { $regex: ?0, $options: 'i' }}, " +
           "{ 'fullName': { $regex: ?0, $options: 'i' }} " +
           "]}")
    Page<User> searchUsers(String keyword, Pageable pageable);
    Page<User> findByRoleAndStatus(UserRole role, UserStatus status, Pageable pageable);
    Page<User> findByRole(UserRole role, Pageable pageable);
    Page<User> findByStatus(UserStatus status, Pageable pageable);

}