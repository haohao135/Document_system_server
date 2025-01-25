package com.document.demo.service;

import com.document.demo.dto.response.ReportDetailResponse;
import com.document.demo.models.enums.DocumentStatus;
import com.document.demo.models.enums.DocumentType;

import java.util.List;

public interface ReportService {
    int getTotalDocument();
    int getCountDocumentByType(DocumentType documentType);
    int getTotalUser();
    double getTotalDocumentGrowthReport();
    double getInComingDocumentGrowthReport();
    double getOutGoingDocumentGrowthReport();
    double getUserGrowthReport();
    long countIncomingDocumentsByMonth(int year, int month);
    long countOutgoingDocumentsByMonth(int year, int month);
    long countIncomingDocumentsByWeek(int year, int month, int weekNumber);
    long countOutgoingDocumentsByWeek(int year, int month, int weekNumber);
    int getDocumentByTypeAndStatus(DocumentType documentType, DocumentStatus documentStatus);
    List<ReportDetailResponse> getReportDetail();
}
