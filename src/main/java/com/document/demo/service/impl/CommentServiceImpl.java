package com.document.demo.service.impl;

import com.document.demo.exception.ResourceNotFoundException;
import com.document.demo.models.Comment;
import com.document.demo.models.Distribution;
import com.document.demo.models.User;
import com.document.demo.models.enums.CommentStatus;
import com.document.demo.repository.CommentRepository;
import com.document.demo.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public Comment createComment(Comment comment) {
        if (comment.getComment() == null || comment.getComment().trim().isEmpty()) {
            throw new IllegalArgumentException("Comment content is required");
        }
        comment.setTimestamp(LocalDateTime.now());
        return commentRepository.save(comment);
    }

    @Override
    @Transactional
    public Comment updateComment(String id, Comment comment) {
        Comment existingComment = findById(id);
        
        if (comment.getComment() != null) {
            existingComment.setComment(comment.getComment());
        }
        existingComment.setTimestamp(LocalDateTime.now());
        existingComment.setStatus(CommentStatus.EDITED);
        
        return commentRepository.save(existingComment);
    }

    @Override
    @Transactional
    public void deleteComment(String id) {
        Comment comment = findById(id);
        commentRepository.delete(comment);
    }

    @Override
    public Comment findById(String id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + id));
    }

    @Override
    public List<Comment> findByUser(User user) {
        return commentRepository.findByUser(user);
    }

    @Override
    public List<Comment> findByDistribution(Distribution distribution) {
        return commentRepository.findByDistribution(distribution);
    }

    @Override
    public List<Comment> findAll() {
        return commentRepository.findAll();
    }
} 