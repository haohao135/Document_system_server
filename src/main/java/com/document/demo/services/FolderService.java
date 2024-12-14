package com.document.demo.services;

import com.document.demo.repository.FolderRepository;
import org.springframework.stereotype.Service;

@Service
public class FolderService{
    private final FolderRepository folderRepository;

    public FolderService(FolderRepository folderRepository) {
        this.folderRepository = folderRepository;
    }
}
