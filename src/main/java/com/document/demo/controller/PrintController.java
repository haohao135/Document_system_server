package com.document.demo.controller;

import com.document.demo.service.PrintService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.print.PrintException;
import java.util.List;

@RestController
@RequestMapping("/api/print")
@RequiredArgsConstructor
@Slf4j
public class PrintController {
    private final PrintService printService;

    @GetMapping("/document/{id}/pdf")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> downloadPDF(@PathVariable String id) throws PrintException {
        log.info("Downloading PDF for document: {}", id);
        byte[] pdfBytes = printService.generatePDF(id);
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=document.pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdfBytes);
    }

    @PostMapping("/documents/excel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> downloadExcel(@RequestBody List<String> documentIds) throws PrintException {
        log.info("Downloading Excel for {} documents", documentIds.size());
        byte[] excelBytes = printService.generateExcel(documentIds);
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=documents.xlsx")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(excelBytes);
    }

    @PostMapping("/document/{id}/print")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> printDocument(
            @PathVariable String id,
            @RequestParam String printerName) throws PrintException {
        log.info("Printing document {} to printer: {}", id, printerName);
        printService.printDocument(id, printerName);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/printers")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<String>> getAvailablePrinters() {
        log.info("Getting available printers");
        return ResponseEntity.ok(printService.getAvailablePrinters());
    }
} 