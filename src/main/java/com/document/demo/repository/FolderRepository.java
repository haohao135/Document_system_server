package com.document.demo.repository;

import com.document.demo.models.Folder;
import com.document.demo.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FolderRepository extends MongoRepository<Folder, String> {
    List<Folder> findByCreatedBy(User user);
    Optional<Folder> findByNameAndCreatedBy(String name, User user);
    boolean existsByNameAndCreatedBy(String name, User user);
} 