package com.document.demo.service;

import com.document.demo.models.enums.DocumentType;

public interface ReportService {
    int getTotalDocument();
    int getCountDocumentByType(DocumentType documentType);
    int getTotalUser();
    double getTotalDocumentGrowthReport();
    double getInComingDocumentGrowthReport();
    double getOutGoingDocumentGrowthReport();
    double getUserGrowthReport();
}
