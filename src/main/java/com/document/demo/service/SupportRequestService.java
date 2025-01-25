package com.document.demo.service;

import com.document.demo.dto.request.SupportUpdateRequest;
import com.document.demo.models.SupportRequest;

import java.util.List;
import java.util.Optional;

public interface SupportRequestService {
    SupportRequest createSupport(SupportRequest supportRequest);
    List<SupportRequest> getAllSupportRequest();
    SupportRequest getSupportRequestById(String id);
    Optional<SupportRequest> updateSupportRequest(String id, SupportUpdateRequest supportUpdateRequest);
}
