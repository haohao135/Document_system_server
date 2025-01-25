package com.document.demo.service.impl;

import com.document.demo.dto.response.ReportDetailResponse;
import com.document.demo.models.enums.DocumentStatus;
import com.document.demo.models.enums.DocumentType;
import com.document.demo.repository.DocumentRepository;
import com.document.demo.repository.UserRepository;
import com.document.demo.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
@Service
@RequiredArgsConstructor
public class ReportServiceImp implements ReportService {
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
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
        try {
            LocalDateTime startOfCurrentMonth = LocalDateTime.now().with(TemporalAdjusters.firstDayOfMonth()).toLocalDate().atStartOfDay();
            LocalDateTime endOfCurrentMonth = LocalDateTime.now().with(TemporalAdjusters.lastDayOfMonth()).toLocalDate().atStartOfDay();

            LocalDateTime startOfPreviousMonth = startOfCurrentMonth.minusMonths(1);
            LocalDateTime endOfPreviousMonth = startOfCurrentMonth.minusDays(1);

            Date startCurrent = Date.from(startOfCurrentMonth.atZone(ZoneId.systemDefault()).toInstant());
            Date endCurrent = Date.from(endOfCurrentMonth.atZone(ZoneId.systemDefault()).toInstant());
            Date startPrevious = Date.from(startOfPreviousMonth.atZone(ZoneId.systemDefault()).toInstant());
            Date endPrevious = Date.from(endOfPreviousMonth.atZone(ZoneId.systemDefault()).toInstant());

            System.out.println("Start Current Month: " + startCurrent);
            System.out.println("End Current Month: " + endCurrent);
            System.out.println("Start Previous Month: " + startPrevious);
            System.out.println("End Previous Month: " + endPrevious);

            long currentTotal = documentRepository.countByDateRange(startCurrent, endCurrent);
            long previousTotal = documentRepository.countByDateRange(startPrevious, endPrevious);

            System.out.println("Current Total: " + currentTotal);
            System.out.println("Previous Total: " + previousTotal);

            return calculatePercentageChange(currentTotal, previousTotal);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public double getInComingDocumentGrowthReport() {
        try{
            LocalDateTime startOfCurrentMonth = LocalDateTime.now().with(TemporalAdjusters.firstDayOfMonth()).toLocalDate().atStartOfDay();
            LocalDateTime endOfCurrentMonth = LocalDateTime.now().with(TemporalAdjusters.lastDayOfMonth()).toLocalDate().atStartOfDay();

            LocalDateTime startOfPreviousMonth = startOfCurrentMonth.minusMonths(1);
            LocalDateTime endOfPreviousMonth = startOfCurrentMonth.minusDays(1);

            Date startCurrent = Date.from(startOfCurrentMonth.atZone(ZoneId.systemDefault()).toInstant());
            Date endCurrent = Date.from(endOfCurrentMonth.atZone(ZoneId.systemDefault()).toInstant());
            Date startPrevious = Date.from(startOfPreviousMonth.atZone(ZoneId.systemDefault()).toInstant());
            Date endPrevious = Date.from(endOfPreviousMonth.atZone(ZoneId.systemDefault()).toInstant());

            long currentIncoming = documentRepository.countByTypeAndDateRange(DocumentType.INCOMING.name(), startCurrent, endCurrent);
            long previousIncoming = documentRepository.countByTypeAndDateRange(DocumentType.INCOMING.name(), startPrevious, endPrevious);

            return calculatePercentageChange(currentIncoming, previousIncoming);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public double getOutGoingDocumentGrowthReport() {
        LocalDateTime startOfCurrentMonth = LocalDateTime.now().with(TemporalAdjusters.firstDayOfMonth()).toLocalDate().atStartOfDay();
        LocalDateTime endOfCurrentMonth = LocalDateTime.now().with(TemporalAdjusters.lastDayOfMonth()).toLocalDate().atStartOfDay();

        LocalDateTime startOfPreviousMonth = startOfCurrentMonth.minusMonths(1);
        LocalDateTime endOfPreviousMonth = startOfCurrentMonth.minusDays(1);

        Date startCurrent = Date.from(startOfCurrentMonth.atZone(ZoneId.systemDefault()).toInstant());
        Date endCurrent = Date.from(endOfCurrentMonth.atZone(ZoneId.systemDefault()).toInstant());
        Date startPrevious = Date.from(startOfPreviousMonth.atZone(ZoneId.systemDefault()).toInstant());
        Date endPrevious = Date.from(endOfPreviousMonth.atZone(ZoneId.systemDefault()).toInstant());

        long currentOutgoing = documentRepository.countByTypeAndDateRange(DocumentType.OUTGOING.name(), startCurrent, endCurrent);
        long previousOutgoing = documentRepository.countByTypeAndDateRange(DocumentType.OUTGOING.name(), startPrevious, endPrevious);

        // Tính phần trăm thay đổi
        return calculatePercentageChange(currentOutgoing, previousOutgoing);
    }


    @Override
    public double getUserGrowthReport() {
        return 0;
    }

    @Override
    public long countIncomingDocumentsByMonth(int year, int month) {
        LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endOfMonth = startOfMonth.with(TemporalAdjusters.lastDayOfMonth()).plusDays(1);

        Date startOfMonthDate = Date.from(startOfMonth.atZone(ZoneId.systemDefault()).toInstant());
        Date endOfMonthDate = Date.from(endOfMonth.atZone(ZoneId.systemDefault()).toInstant());

        return documentRepository.countByTypeAndDateRange(DocumentType.INCOMING.name(), startOfMonthDate, endOfMonthDate);
    }

    @Override
    public long countOutgoingDocumentsByMonth(int year, int month) {
        LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endOfMonth = startOfMonth.with(TemporalAdjusters.lastDayOfMonth()).plusDays(1);

        Date startOfMonthDate = Date.from(startOfMonth.atZone(ZoneId.systemDefault()).toInstant());
        Date endOfMonthDate = Date.from(endOfMonth.atZone(ZoneId.systemDefault()).toInstant());

        return documentRepository.countByTypeAndDateRange(DocumentType.OUTGOING.name(), startOfMonthDate, endOfMonthDate);
    }


    @Override
    public long countIncomingDocumentsByWeek(int year, int month, int weekNumber) {
        LocalDateTime firstDayOfMonth = LocalDateTime.of(year, month, 1, 0, 0, 0);

        LocalDateTime startOfWeek = firstDayOfMonth
                .with(WeekFields.of(Locale.getDefault()).weekOfMonth(), weekNumber)
                .with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1);

        LocalDateTime endOfWeek = startOfWeek.plusDays(6).withHour(23).withMinute(59).withSecond(59);

        Date startOfWeekDate = Date.from(startOfWeek.atZone(ZoneId.systemDefault()).toInstant());
        Date endOfWeekDate = Date.from(endOfWeek.atZone(ZoneId.systemDefault()).toInstant());

        return documentRepository.countByTypeAndDateRange(DocumentType.INCOMING.name(), startOfWeekDate, endOfWeekDate);
    }

    @Override
    public long countOutgoingDocumentsByWeek(int year, int month, int weekNumber) {
        LocalDateTime firstDayOfMonth = LocalDateTime.of(year, month, 1, 0, 0, 0);

        LocalDateTime startOfWeek = firstDayOfMonth
                .with(WeekFields.of(Locale.getDefault()).weekOfMonth(), weekNumber)
                .with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1);

        LocalDateTime endOfWeek = startOfWeek.plusDays(6).withHour(23).withMinute(59).withSecond(59);

        Date startOfWeekDate = Date.from(startOfWeek.atZone(ZoneId.systemDefault()).toInstant());
        Date endOfWeekDate = Date.from(endOfWeek.atZone(ZoneId.systemDefault()).toInstant());

        return documentRepository.countByTypeAndDateRange(DocumentType.OUTGOING.name(), startOfWeekDate, endOfWeekDate);
    }

    public int getDocumentByTypeAndStatus(DocumentType documentType, DocumentStatus documentStatus){
        return (int) documentRepository.countByTypeAndStatus(documentType, documentStatus);
    }

    @Override
    public List<ReportDetailResponse> getReportDetail() {
        ReportDetailResponse rp1 = new ReportDetailResponse(
                DocumentType.INCOMING,
                getCountDocumentByType(DocumentType.INCOMING),
                getDocumentByTypeAndStatus(DocumentType.INCOMING, DocumentStatus.PENDING),
                getDocumentByTypeAndStatus(DocumentType.INCOMING, DocumentStatus.PROCESSING),
                getDocumentByTypeAndStatus(DocumentType.INCOMING, DocumentStatus.COMPLETED)
        );
        ReportDetailResponse rp2 = new ReportDetailResponse(
                DocumentType.OUTGOING,
                getCountDocumentByType(DocumentType.OUTGOING),
                getDocumentByTypeAndStatus(DocumentType.OUTGOING, DocumentStatus.PENDING),
                getDocumentByTypeAndStatus(DocumentType.OUTGOING, DocumentStatus.PROCESSING),
                getDocumentByTypeAndStatus(DocumentType.OUTGOING, DocumentStatus.COMPLETED)
        );
        ReportDetailResponse rp3 = new ReportDetailResponse(
                DocumentType.INTERNAL,
                getCountDocumentByType(DocumentType.INTERNAL),
                getDocumentByTypeAndStatus(DocumentType.INTERNAL, DocumentStatus.PENDING),
                getDocumentByTypeAndStatus(DocumentType.INTERNAL, DocumentStatus.PROCESSING),
                getDocumentByTypeAndStatus(DocumentType.INTERNAL, DocumentStatus.COMPLETED)
        );
        List<ReportDetailResponse> list = new ArrayList<>();
        list.add(rp1);
        list.add(rp2);
        list.add(rp3);
        return list;
    }

    private double calculatePercentageChange(long current, long previous) {
        if (previous == 0) {
            return current > 0 ? 100.0 : 0.0;
        }
        return ((double) (current - previous) / previous) * 100;
    }
}
