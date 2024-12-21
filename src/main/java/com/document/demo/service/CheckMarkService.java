package com.document.demo.service;

import com.document.demo.models.CheckMark;
import com.document.demo.models.Documents;
import com.document.demo.models.Folder;

import java.util.List;

public interface CheckMarkService {
    CheckMark createCheckMark(CheckMark checkMark);
    CheckMark updateCheckMark(String id, CheckMark checkMark);
    void deleteCheckMark(String id);
    CheckMark findById(String id);
    List<CheckMark> findByFolder(Folder folder);
    List<CheckMark> findByDocument(Documents document);
    List<CheckMark> findAll();
} 