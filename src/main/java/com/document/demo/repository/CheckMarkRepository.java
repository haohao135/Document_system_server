package com.document.demo.repository;

import com.document.demo.models.CheckMark;
import com.document.demo.models.Documents;
import com.document.demo.models.Folder;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CheckMarkRepository extends MongoRepository<CheckMark, String> {
    List<CheckMark> findByFolder(Folder folder);
    List<CheckMark> findByDocument(Documents document);
    Optional<CheckMark> findByName(String name);
} 