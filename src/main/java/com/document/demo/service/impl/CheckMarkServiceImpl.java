package com.document.demo.service.impl;

import com.document.demo.dto.request.TrackingRequest;
import com.document.demo.exception.ResourceNotFoundException;
import com.document.demo.models.CheckMark;
import com.document.demo.models.Documents;
import com.document.demo.models.Folder;
import com.document.demo.models.enums.TrackingActionType;
import com.document.demo.models.enums.TrackingEntityType;
import com.document.demo.models.tracking.ChangeLog;
import com.document.demo.repository.CheckMarkRepository;
import com.document.demo.service.CheckMarkService;
import com.document.demo.service.TrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import static com.document.demo.utils.UpdateFieldUtils.updateField;

@Service
@Slf4j
@RequiredArgsConstructor
public class CheckMarkServiceImpl implements CheckMarkService {
    private final CheckMarkRepository checkMarkRepository;
    private final TrackingService trackingService;
    private final UserServiceImpl userService;

    @Override
    @Transactional
    public CheckMark createCheckMark(CheckMark checkMark) {
        if (checkMark.getName() == null || checkMark.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("CheckMark name is required");
        }

        CheckMark savedCheckMark = checkMarkRepository.save(checkMark);
        
        // Track checkmark creation
        trackingService.track(TrackingRequest.builder()
            .actor(userService.getCurrentUser())
            .entityType(TrackingEntityType.CHECKMARK)
            .entityId(savedCheckMark.getCheckMarkId())
            .action(TrackingActionType.CREATE)
            .metadata(Map.of(
                "name", savedCheckMark.getName(),
                "folderId", savedCheckMark.getFolder().getFolderId(),
                "documentId", savedCheckMark.getDocument().getDocumentId()
            ))
            .build());

        return savedCheckMark;
    }

    @Override
    @Transactional 
    public CheckMark updateCheckMark(String id, CheckMark checkMark) {
        CheckMark existingCheckMark = findById(id);
        
        Map<String, ChangeLog> changes = new HashMap<>();
        updateField(changes, "name", existingCheckMark.getName(), checkMark.getName(), existingCheckMark::setName);

        CheckMark updatedCheckMark = checkMarkRepository.save(existingCheckMark);
        
        // Track checkmark update
        trackingService.track(TrackingRequest.builder()
            .actor(userService.getCurrentUser())
            .entityType(TrackingEntityType.CHECKMARK)
            .entityId(id)
            .action(TrackingActionType.UPDATE)
            .changes(changes)
            .build());
            
        return updatedCheckMark;
    }

    @Override
    @Transactional
    public void deleteCheckMark(String id) {
        CheckMark checkMark = findById(id);
        
        // Track checkmark deletion before deleting
        trackingService.track(TrackingRequest.builder()
            .actor(userService.getCurrentUser())
            .entityType(TrackingEntityType.CHECKMARK)
            .entityId(id)
            .action(TrackingActionType.DELETE)
            .metadata(Map.of(
                "name", checkMark.getName(),
                "folderId", checkMark.getFolder().getFolderId(),
                "documentId", checkMark.getDocument().getDocumentId()
            ))
            .build());
            
        checkMarkRepository.deleteById(id);
    }

    @Override
    public CheckMark findById(String id) {
        return checkMarkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CheckMark not found with id: " + id));
    }

    @Override
    public List<CheckMark> findByFolder(Folder folder) {
        return checkMarkRepository.findByFolder(folder);
    }

    @Override
    public List<CheckMark> findByDocument(Documents document) {
        return checkMarkRepository.findByDocument(document);
    }

    @Override
    public List<CheckMark> findAll() {
        return checkMarkRepository.findAll();
    }
} 