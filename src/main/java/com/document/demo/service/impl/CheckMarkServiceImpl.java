package com.document.demo.service.impl;

import com.document.demo.exception.ResourceNotFoundException;
import com.document.demo.models.CheckMark;
import com.document.demo.models.Documents;
import com.document.demo.models.Folder;
import com.document.demo.repository.CheckMarkRepository;
import com.document.demo.service.CheckMarkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CheckMarkServiceImpl implements CheckMarkService {
    private final CheckMarkRepository checkMarkRepository;

    @Override
    @Transactional
    public CheckMark createCheckMark(CheckMark checkMark) {
        // Basic validation
        if (checkMark.getName() == null || checkMark.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("CheckMark name is required");
        }

        // Save checkmark
        return checkMarkRepository.save(checkMark);
    }

    @Override
    @Transactional 
    public CheckMark updateCheckMark(String id, CheckMark checkMark) {
        CheckMark existingCheckMark = findById(id);
        existingCheckMark.setName(checkMark.getName());
        return checkMarkRepository.save(existingCheckMark);
    }

    @Override
    @Transactional
    public void deleteCheckMark(String id) {
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