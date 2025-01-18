package com.document.demo.controller;

import com.document.demo.dto.response.*;
import com.document.demo.models.enums.DocumentStatus;
import com.document.demo.models.enums.DocumentType;
import com.document.demo.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {
   private final ReportService reportService;
   @GetMapping("/get-total-document")
    public ResponseEntity<?> getTotalAllDocument(){
        try {
            int count = reportService.getTotalDocument();
            return ResponseEntity.ok(
                    new SuccessResponse(
                            "Get all document success",
                            count
                    )
            );
        } catch (Exception e){
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error get all document: " + e.getMessage()));
        }
    }
    @GetMapping("/get-total-incoming-document")
    public ResponseEntity<?> getAllInComingDocument(){
        try {
            int count = reportService.getCountDocumentByType(DocumentType.INCOMING);
            return ResponseEntity.ok(
                    new SuccessResponse(
                            "Get all incoming document success",
                            count
                    )
            );
        } catch (Exception e){
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error get all incoming document: " + e.getMessage()));
        }
    }
    @GetMapping("/get-total-outgoing-document")
    public ResponseEntity<?> getAllOutGoingDocument(){
        try {
            int count = reportService.getCountDocumentByType(DocumentType.OUTGOING);
            return ResponseEntity.ok(
                    new SuccessResponse(
                            "Get all outgoing document success",
                            count
                    )
            );
        } catch (Exception e){
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error get all outgoing document: " + e.getMessage()));
        }
    }
    @GetMapping("/get-all-users")
    public ResponseEntity<?> getAllUser(){
        try {
            int count = reportService.getTotalUser();
            return ResponseEntity.ok(
                    new SuccessResponse(
                            "Get all users success",
                            count
                    )
            );
        } catch (Exception e){
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error get all users: " + e.getMessage()));
        }
    }
    @GetMapping("/get-percent-all-document")
    public ResponseEntity<?> getPercentageAllDocuments() {
        System.out.println("API getPercentageAllDocuments is called");

        try {
            double per = reportService.getTotalDocumentGrowthReport();
            System.out.println("Document growth percentage: " + per);

            return ResponseEntity.ok(
                    new SuccessResponse(
                            "Get percentage document success",
                            per
                    )
            );
        } catch (Exception e) {
            System.err.println("Error occurred in getPercentageAllDocuments");
            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error get percentage document: " + e.getMessage()));
        }
    }

    @GetMapping("/get-percent-incoming-document")
    public ResponseEntity<?> getPercentageInComingDocuments(){
        try {
            double per = reportService.getInComingDocumentGrowthReport();
            return ResponseEntity.ok(
                    new SuccessResponse(
                            "Get percentage incoming document success",
                            per
                    )
            );
        } catch (Exception e){
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error get percentage incoming document: " + e.getMessage()));
        }
    }
    @GetMapping("/get-percent-outgoing-document")
    public ResponseEntity<?> getPercentageOutgoingDocuments(){
        try {
            double per = reportService.getOutGoingDocumentGrowthReport();
            return ResponseEntity.ok(
                    new SuccessResponse(
                            "Get percentage outgoing document success",
                            per
                    )
            );
        } catch (Exception e){
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error get percentage outgoing document: " + e.getMessage()));
        }
    }
    @GetMapping("/get-monthly-statistic-document")
    public ResponseEntity<?> getMonthlyStatisticByDocument(@RequestParam int year, @RequestParam int month){
        try {
            long ic = reportService.countIncomingDocumentsByMonth(year, month);
            long o = reportService.countOutgoingDocumentsByMonth(year, month);
            DocumentStatisticByMonthResponse a = new DocumentStatisticByMonthResponse();
            a.setMonth(month);
            a.setCountIncomingDocument((int) ic);
            a.setCountOutgoingDocument((int) o);
            return ResponseEntity.ok(
                    new SuccessResponse(
                            "Get statistic by document success",
                            a
                    )
            );
        } catch (Exception e){
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error get statistic by document: " + e.getMessage()));
        }
    }
    @GetMapping("/get-week-statistic-document")
    public ResponseEntity<?> getWeeksStatisticByDocument(@RequestParam int year, @RequestParam int month, @RequestParam int week){
        try {
            double per = reportService.getOutGoingDocumentGrowthReport();
            long ic = reportService.countIncomingDocumentsByWeek(year, month, week);
            long o = reportService.countOutgoingDocumentsByWeek(year, month, week);
            DocumentStatisticByWeekResponse a = new DocumentStatisticByWeekResponse();
            a.setWeek(week);
            a.setCountIncomingDocument((int) ic);
            a.setCountOutgoingDocument((int) o);
            return ResponseEntity.ok(
                    new SuccessResponse(
                            "Get statistic by document success",
                            a
                    )
            );
        } catch (Exception e){
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error get statistic by document: " + e.getMessage()));
        }
    }
    @GetMapping("/get-report-detail")
    public ResponseEntity<?> getReportDetail(){
        try {
            List<ReportDetailResponse> list = reportService.getReportDetail();
            return ResponseEntity.ok(
                    new SuccessResponse(
                            "Get report detail success",
                            list
                    )
            );
        } catch (Exception e){
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error get report detail: " + e.getMessage()));
        }
    }

}
