package com.document.demo.service;

import com.document.demo.models.Distribution;
import com.document.demo.models.Documents;
import com.document.demo.models.User;
import com.document.demo.models.enums.DistributionStatus;

import java.util.List;

public interface DistributionService {
    Distribution createDistribution(Distribution distribution);
    Distribution updateDistribution(String id, Distribution distribution);
    void deleteDistribution(String id);
    Distribution findById(String id);
    List<Distribution> findBySender(User sender);
    List<Distribution> findByReceiver(User receiver);
    List<Distribution> findByStatus(DistributionStatus status);
    Distribution findByDocuments(Documents document);
    List<Distribution> findAll();
} 