package com.document.demo.service;

import com.document.demo.dto.request.SupportUpdateRequest;
import com.document.demo.models.Support;

import java.util.List;
import java.util.Optional;

public interface SupportRequestService {
    Support createSupport(Support supportRequest);
    List<Support> getAllSupportRequest();
    Support getSupportRequestById(String id);
    Optional<Support> updateSupportRequest(String id, SupportUpdateRequest supportUpdateRequest);
}
