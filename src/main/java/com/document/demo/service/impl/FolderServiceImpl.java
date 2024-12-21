package com.document.demo.service.impl;

import com.document.demo.exception.ResourceAlreadyExistsException;
import com.document.demo.exception.ResourceNotFoundException;
import com.document.demo.models.Folder;
import com.document.demo.models.User;
import com.document.demo.repository.FolderRepository;
import com.document.demo.service.FolderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FolderServiceImpl implements FolderService {
    private final FolderRepository folderRepository;

    @Override
    @Transactional
    public Folder createFolder(Folder folder) {
        if (existsByNameAndCreatedBy(folder.getName(), folder.getCreatedBy())) {
            throw new ResourceAlreadyExistsException("Folder with this name already exists");
        }

        return folderRepository.save(folder);
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

        if (folder.getName() != null) {
            existingFolder.setName(folder.getName());
        }

        return folderRepository.save(existingFolder);
    }

    @Override
    @Transactional
    public void deleteFolder(String id) {
        Folder folder = findById(id);
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