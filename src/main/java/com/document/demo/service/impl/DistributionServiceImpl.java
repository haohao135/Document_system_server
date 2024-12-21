package com.document.demo.service.impl;

import com.document.demo.models.Distribution;
import com.document.demo.models.Documents;
import com.document.demo.models.User;
import com.document.demo.models.enums.DistributionStatus;
import com.document.demo.exception.ResourceNotFoundException;
import com.document.demo.repository.DistributionRepository;
import com.document.demo.service.DistributionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DistributionServiceImpl implements DistributionService {
    private final DistributionRepository distributionRepository;

    @Override
    @Transactional
    public Distribution createDistribution(Distribution distribution) {
        distribution.setTimestamp(LocalDateTime.now());
        if (distribution.getStatus() == null) {
            distribution.setStatus(DistributionStatus.PENDING);
        }
        return distributionRepository.save(distribution);
    }

    @Override
    @Transactional
    public Distribution updateDistribution(String id, Distribution distribution) {
        Distribution existingDistribution = findById(id);

        if (distribution.getNote() != null) {
            existingDistribution.setNote(distribution.getNote());
        }
        if (distribution.getStatus() != null) {
            existingDistribution.setStatus(distribution.getStatus());
        }
        if (distribution.getReceivers() != null) {
            existingDistribution.setReceivers(distribution.getReceivers());
        }

        return distributionRepository.save(existingDistribution);
    }

    @Override
    @Transactional
    public void deleteDistribution(String id) {
        Distribution distribution = findById(id);
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