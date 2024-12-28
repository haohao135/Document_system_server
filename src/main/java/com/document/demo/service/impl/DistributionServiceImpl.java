package com.document.demo.service.impl;

import com.document.demo.dto.request.TrackingRequest;
import com.document.demo.models.Distribution;
import com.document.demo.models.Documents;
import com.document.demo.models.User;
import com.document.demo.models.enums.DistributionStatus;
import com.document.demo.exception.ResourceNotFoundException;
import com.document.demo.models.enums.TrackingActionType;
import com.document.demo.models.enums.TrackingEntityType;
import com.document.demo.models.tracking.ChangeLog;
import com.document.demo.repository.DistributionRepository;
import com.document.demo.service.DistributionService;
import com.document.demo.service.TrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import static com.document.demo.utils.UpdateFieldUtils.updateField;

@Service
@Slf4j
@RequiredArgsConstructor
public class DistributionServiceImpl implements DistributionService {
    private final DistributionRepository distributionRepository;
    private final TrackingService trackingService;
    private final UserServiceImpl userServiceImpl;

    @Override
    @Transactional
    public Distribution createDistribution(Distribution distribution) {
        distribution.setTimestamp(LocalDateTime.now());
        if (distribution.getStatus() == null) {
            distribution.setStatus(DistributionStatus.PENDING);
        }
        
        Distribution savedDistribution = distributionRepository.save(distribution);
        
        // Track distribution creation
        trackingService.track(TrackingRequest.builder()
            .actor(userServiceImpl.getCurrentUser())
            .entityType(TrackingEntityType.DISTRIBUTION)
            .entityId(savedDistribution.getDistributionId())
            .action(TrackingActionType.CREATE)
            .metadata(Map.of(
                "documentId", savedDistribution.getDocuments().getDocumentId(),
                "status", savedDistribution.getStatus().toString(),
                "receiversCount", String.valueOf(savedDistribution.getReceivers().size())
            ))
            .build());
            
        return savedDistribution;
    }

    @Override
    @Transactional
    public Distribution updateDistribution(String id, Distribution distribution) {
        Distribution existingDistribution = findById(id);
        
        Map<String, ChangeLog> changes = new HashMap<>();
        updateField(changes, "note", distribution.getNote(), existingDistribution.getNote(), existingDistribution::setNote);
        updateField(changes, "timestamp", distribution.getTimestamp(), existingDistribution.getTimestamp(), existingDistribution::setTimestamp);
        updateField(changes, "status", distribution.getStatus(), existingDistribution.getStatus(), existingDistribution::setStatus);
        updateField(changes, "sender", distribution.getSender(), existingDistribution.getSender(), existingDistribution::setSender);
        updateField(changes, "documents", distribution.getDocuments(), existingDistribution.getDocuments(), existingDistribution::setDocuments);
        updateField(changes, "receivers", distribution.getReceivers(), existingDistribution.getReceivers(), existingDistribution::setReceivers);

        Distribution updatedDistribution = distributionRepository.save(existingDistribution);
        
        // Track distribution update
        trackingService.track(TrackingRequest.builder()
            .actor(userServiceImpl.getCurrentUser())
            .entityType(TrackingEntityType.DISTRIBUTION)
            .entityId(id)
            .action(TrackingActionType.UPDATE)
            .changes(changes)
            .build());
            
        return updatedDistribution;
    }

    @Override
    @Transactional
    public void deleteDistribution(String id) {
        Distribution distribution = findById(id);
        
        // Track distribution deletion
        trackingService.track(TrackingRequest.builder()
            .actor(userServiceImpl.getCurrentUser())
            .entityType(TrackingEntityType.DISTRIBUTION)
            .entityId(id)
            .action(TrackingActionType.DELETE)
            .metadata(Map.of(
                "documentId", distribution.getDocuments().getDocumentId(),
                "status", distribution.getStatus().toString()
            ))
            .build());
            
        distributionRepository.delete(distribution);
    }

    @Override
    public Distribution findById(String id) {
        return distributionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Distribution not found with id: " + id));
    }

    @Override
    public List<Distribution> findBySender(User sender) {
        return distributionRepository.findBySender(sender);
    }

    @Override
    public List<Distribution> findByReceiver(User receiver) {
        return distributionRepository.findByReceiversContaining(receiver);
    }

    @Override
    public List<Distribution> findByStatus(DistributionStatus status) {
        return distributionRepository.findByStatus(status);
    }

    @Override
    public Distribution findByDocuments(Documents document) {
        return distributionRepository.findByDocuments(document);
    }

    @Override
    public List<Distribution> findAll() {
        return distributionRepository.findAll();
    }
} 