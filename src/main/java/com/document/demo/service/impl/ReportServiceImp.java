package com.document.demo.service.impl;

import com.document.demo.models.enums.DocumentType;
import com.document.demo.repository.DocumentRepository;
import com.document.demo.repository.UserRepository;
import com.document.demo.service.ReportService;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

public class ReportServiceImp implements ReportService {
    DocumentRepository documentRepository;
    UserRepository userRepository;
    @Override
    public int getTotalDocument() {
        return documentRepository.findAll().size();
    }

    @Override
    public int getCountDocumentByType(DocumentType documentType) {
        return (int) documentRepository.countByType(documentType);
    }

    @Override
    public int getTotalUser() {
        return userRepository.findAll().size();
    }

    @Override
    public double getTotalDocumentGrowthReport() {
        LocalDateTime startOfCurrentMonth = LocalDateTime.now().with(TemporalAdjusters.firstDayOfMonth()).toLocalDate().atStartOfDay();
        LocalDateTime endOfCurrentMonth = LocalDateTime.now().with(TemporalAdjusters.lastDayOfMonth()).toLocalDate().atStartOfDay();

        LocalDateTime startOfPreviousMonth = startOfCurrentMonth.minusMonths(1);
        LocalDateTime endOfPreviousMonth = startOfCurrentMonth.minusDays(1);

        long currentTotal = documentRepository.countByDateRange(startOfCurrentMonth, endOfCurrentMonth);
        long previousTotal = documentRepository.countByDateRange(startOfPreviousMonth, endOfPreviousMonth);
        return calculatePercentageChange(currentTotal, previousTotal);
    }

    @Override
    public double getInComingDocumentGrowthReport() {
        LocalDateTime startOfCurrentMonth = LocalDateTime.now().with(TemporalAdjusters.firstDayOfMonth()).toLocalDate().atStartOfDay();
        LocalDateTime endOfCurrentMonth = LocalDateTime.now().with(TemporalAdjusters.lastDayOfMonth()).toLocalDate().atStartOfDay();

        LocalDateTime startOfPreviousMonth = startOfCurrentMonth.minusMonths(1);
        LocalDateTime endOfPreviousMonth = startOfCurrentMonth.minusDays(1);

        long currentIncoming = documentRepository.countByTypeAndDateRange(DocumentType.INCOMING, startOfCurrentMonth, endOfCurrentMonth);
        long previousIncoming = documentRepository.countByTypeAndDateRange(DocumentType.INCOMING, startOfPreviousMonth, endOfPreviousMonth);
        return calculatePercentageChange(currentIncoming, previousIncoming);
    }

    @Override
    public double getOutGoingDocumentGrowthReport() {
        LocalDateTime startOfCurrentMonth = LocalDateTime.now().with(TemporalAdjusters.firstDayOfMonth()).toLocalDate().atStartOfDay();
        LocalDateTime endOfCurrentMonth = LocalDateTime.now().with(TemporalAdjusters.lastDayOfMonth()).toLocalDate().atStartOfDay();

        LocalDateTime startOfPreviousMonth = startOfCurrentMonth.minusMonths(1);
        LocalDateTime endOfPreviousMonth = startOfCurrentMonth.minusDays(1);

        long currentOutgoing = documentRepository.countByTypeAndDateRange(DocumentType.OUTGOING, startOfCurrentMonth, endOfCurrentMonth);
        long previousOutgoing = documentRepository.countByTypeAndDateRange(DocumentType.OUTGOING, startOfPreviousMonth, endOfPreviousMonth);
        return calculatePercentageChange(currentOutgoing, previousOutgoing);
    }

    @Override
    public double getUserGrowthReport() {
        return 0;
    }
    private double calculatePercentageChange(long current, long previous) {
        if (previous == 0) {
            return current > 0 ? 100.0 : 0.0;
        }
        return ((double) (current - previous) / previous) * 100;
    }
}
