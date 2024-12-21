package com.document.demo.service;

import com.document.demo.models.Folder;
import com.document.demo.models.User;

import java.util.List;

public interface FolderService {
    Folder createFolder(Folder folder);
    Folder updateFolder(String id, Folder folder);
    void deleteFolder(String id);
    Folder findById(String id);
    List<Folder> findByCreatedBy(User user);
    List<Folder> findAll();
    boolean existsByNameAndCreatedBy(String name, User user);

    void save(Folder folder);
} 