package com.document.demo.service.impl;

import com.document.demo.dto.request.TrackingRequest;
import com.document.demo.exception.ResourceNotFoundException;
import com.document.demo.models.Comment;
import com.document.demo.models.Distribution;
import com.document.demo.models.User;
import com.document.demo.models.enums.CommentStatus;
import com.document.demo.models.enums.TrackingActionType;
import com.document.demo.models.enums.TrackingEntityType;
import com.document.demo.models.tracking.ChangeLog;
import com.document.demo.repository.CommentRepository;
import com.document.demo.service.CommentService;
import com.document.demo.service.TrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.document.demo.utils.UpdateFieldUtils.updateField;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final TrackingService trackingService;

    @Override
    @Transactional
    public Comment createComment(Comment comment) {
        if (comment.getComment() == null || comment.getComment().trim().isEmpty()) {
            throw new IllegalArgumentException("Comment content is required");
        }
        comment.setTimestamp(LocalDateTime.now());
        Comment savedComment = commentRepository.save(comment);
        
        trackingService.track(TrackingRequest.builder()
            .actor(comment.getUser())
            .entityType(TrackingEntityType.COMMENT)
            .entityId(savedComment.getCommentId())
            .action(TrackingActionType.CREATE)
            .metadata(Map.of(
                "distributionId", comment.getDistribution().getDistributionId(),
                "content", comment.getComment()
            ))
            .build());
            
        return savedComment;
    }

    @Override
    @Transactional
    public Comment updateComment(String id, Comment comment) {
        Comment existingComment = findById(id);

        Map<String, ChangeLog> changes = new HashMap<>();
        updateField(changes, "comment", existingComment.getComment(), comment.getComment(), existingComment::setComment);
        updateField(changes, "status", existingComment.getStatus(), CommentStatus.EDITED, existingComment::setStatus);
        updateField(changes, "timestamp", existingComment.getTimestamp(), LocalDateTime.now(), existingComment::setTimestamp);

        trackingService.track(TrackingRequest.builder()
            .actor(existingComment.getUser())
            .entityType(TrackingEntityType.COMMENT)
            .entityId(existingComment.getCommentId())
            .action(TrackingActionType.UPDATE)
            .changes(changes)
            .metadata(Map.of(
                "distributionId", existingComment.getDistribution().getDistributionId(),
                "content", existingComment.getComment()
            ))
            .build());
        
        return commentRepository.save(existingComment);
    }

    @Override
    @Transactional
    public void deleteComment(String id) {
        Comment comment = findById(id);

        trackingService.track(TrackingRequest.builder()
            .actor(comment.getUser())
            .entityType(TrackingEntityType.COMMENT)
            .entityId(comment.getCommentId())
            .action(TrackingActionType.DELETE)
            .metadata(Map.of(
                "distributionId", comment.getDistribution().getDistributionId(),
                "content", comment.getComment()
            ))
            .build());

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