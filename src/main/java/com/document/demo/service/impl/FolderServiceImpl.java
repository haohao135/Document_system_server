package com.document.demo.service.impl;

import com.document.demo.dto.request.TrackingRequest;
import com.document.demo.exception.ResourceAlreadyExistsException;
import com.document.demo.exception.ResourceNotFoundException;
import com.document.demo.models.Folder;
import com.document.demo.models.User;
import com.document.demo.models.enums.TrackingActionType;
import com.document.demo.models.enums.TrackingEntityType;
import com.document.demo.models.tracking.ChangeLog;
import com.document.demo.repository.FolderRepository;
import com.document.demo.service.FolderService;
import com.document.demo.service.TrackingService;
import com.document.demo.utils.SecurityUtils;
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
public class FolderServiceImpl implements FolderService {
    private final FolderRepository folderRepository;
    private final TrackingService trackingService;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional
    public Folder createFolder(Folder folder) {
        if (existsByNameAndCreatedBy(folder.getName(), folder.getCreatedBy())) {
            throw new ResourceAlreadyExistsException("Folder with this name already exists");
        }

        Folder savedFolder = folderRepository.save(folder);
        
        // Track folder creation
        trackingService.track(TrackingRequest.builder()
            .actor(securityUtils.getCurrentUser())
            .entityType(TrackingEntityType.FOLDER)
            .entityId(savedFolder.getFolderId())
            .action(TrackingActionType.CREATE)
            .metadata(Map.of(
                "name", savedFolder.getName(),
                "createdBy", savedFolder.getCreatedBy().getUsername()
            ))
            .build());

        return savedFolder;
    }

    @Override
    @Transactional
    public Folder updateFolder(String id, Folder folder) {
        Folder existingFolder = findById(id);
        
        if (folder.getName() != null && 
            !folder.getName().equals(existingFolder.getName()) && 
            existsByNameAndCreatedBy(folder.getName(), existingFolder.getCreatedBy())) {
            throw new ResourceAlreadyExistsException("Folder with this name already exists");
        }

        Map<String, ChangeLog> changes = new HashMap<>();
        updateField(changes, "name", existingFolder.getName(), folder.getName(), existingFolder::setName);

        Folder updatedFolder = folderRepository.save(existingFolder);
        
        // Track folder update
        trackingService.track(TrackingRequest.builder()
            .actor(securityUtils.getCurrentUser())
            .entityType(TrackingEntityType.FOLDER)
            .entityId(id)
            .action(TrackingActionType.UPDATE)
            .changes(changes)
            .build());

        return updatedFolder;
    }

    @Override
    @Transactional
    public void deleteFolder(String id) {
        Folder folder = findById(id);
        
        // Track folder deletion
        trackingService.track(TrackingRequest.builder()
            .actor(securityUtils.getCurrentUser())
            .entityType(TrackingEntityType.FOLDER)
            .entityId(id)
            .action(TrackingActionType.DELETE)
            .metadata(Map.of(
                "name", folder.getName(),
                "createdBy", folder.getCreatedBy().getUsername()
            ))
            .build());
            
        folderRepository.delete(folder);
    }

    @Override
    public Folder findById(String id) {
        return folderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Folder not found with id: " + id));
    }

    @Override
    public List<Folder> findByCreatedBy(User user) {
        return folderRepository.findByCreatedBy(user);
    }

    @Override
    public List<Folder> findAll() {
        return folderRepository.findAll();
    }

    @Override
    public boolean existsByNameAndCreatedBy(String name, User user) {
        return folderRepository.existsByNameAndCreatedBy(name, user);
    }

    @Override
    public void save(Folder folder) {
        folderRepository.save(folder);
    }
} 