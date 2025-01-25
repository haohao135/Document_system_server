package com.document.demo.service.impl;

import com.document.demo.dto.request.SupportUpdateRequest;
import com.document.demo.exception.ResourceNotFoundException;
import com.document.demo.models.Support;
import com.document.demo.repository.SupportRequestRepository;
import com.document.demo.service.SupportRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class SupportRequestServiceImp implements SupportRequestService {
    @Autowired
    private SupportRequestRepository supportRequestRepository;

    @Override
    public Support createSupport(Support supportRequest) {
        return supportRequestRepository.save(supportRequest);
    }

    @Override
    public List<Support> getAllSupportRequest() {
        return supportRequestRepository.findAll();
    }

    @Override
    public Support getSupportRequestById(String id) {
        return supportRequestRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Not found support"));
    }

    @Override
    public Optional<Support> updateSupportRequest(String id, SupportUpdateRequest supportUpdateRequest) {
        return supportRequestRepository.findById(id).map(
                existingRequest -> {
                    existingRequest.setTitle(supportUpdateRequest.getTitle());
                    existingRequest.setType(supportUpdateRequest.getType());
                    existingRequest.setStatus(supportUpdateRequest.getStatus());
                    existingRequest.setContent(supportUpdateRequest.getContent());
                    return supportRequestRepository.save(existingRequest);
                }
        );
    }
}
