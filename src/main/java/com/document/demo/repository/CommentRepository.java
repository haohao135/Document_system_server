package com.document.demo.repository;

import com.document.demo.models.Comment;
import com.document.demo.models.Distribution;
import com.document.demo.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CommentRepository extends MongoRepository<Comment, String> {
    List<Comment> findByUser(User user);
    List<Comment> findByDistribution(Distribution distribution);
    List<Comment> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
} 