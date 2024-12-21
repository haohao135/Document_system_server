package com.document.demo.service;

import com.document.demo.models.Comment;
import com.document.demo.models.Distribution;
import com.document.demo.models.User;

import java.util.List;

public interface CommentService {
    Comment createComment(Comment comment);
    Comment updateComment(String id, Comment comment);
    void deleteComment(String id);
    Comment findById(String id);
    List<Comment> findByUser(User user);
    List<Comment> findByDistribution(Distribution distribution);
    List<Comment> findAll();
} 