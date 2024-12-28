package com.document.demo.service.impl;

import com.document.demo.dto.request.TrackingRequest;
import com.document.demo.exception.PrintException;
import com.document.demo.models.Documents;
import com.document.demo.models.enums.TrackingActionType;
import com.document.demo.models.enums.TrackingEntityType;
import com.document.demo.service.DocumentService;
import com.document.demo.service.PrintService;
import com.document.demo.service.TrackingService;
import com.document.demo.service.CloudinaryService;
import com.itextpdf.text.Font;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PrintServiceImpl implements PrintService {
    private final DocumentService documentService;
    private final TrackingService trackingService;
    private final UserServiceImpl userService;
    private final CloudinaryService cloudinaryService;

    @Override
    public byte[] generatePDF(String documentId) throws PrintException {
        log.info("Generating PDF for document: {}", documentId);
        Documents document = documentService.findById(documentId);
        
        byte[] fileContent;
        try {
            fileContent = cloudinaryService.downloadFile(document.getAttachment());
        } catch (IOException e) {
            log.error("Failed to download file from Cloudinary for document: {}", documentId, e);
            throw new PrintException("Failed to download document file", e);
        }
        
        byte[] pdfBytes = generatePDFContent(document, fileContent);
        trackDocumentOperation(document, "PDF", TrackingActionType.PRINT);
        return pdfBytes;
    }

    private byte[] generatePDFContent(Documents document, byte[] fileContent) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document pdf = new Document(PageSize.A4);
            PdfWriter.getInstance(pdf, baos);
            pdf.open();

            addDocumentContent(pdf, document);
            pdf.add(new Paragraph(new String(fileContent)));

            pdf.close();
            return baos.toByteArray();
        } catch (DocumentException | IOException e) {
            log.error("Failed to generate PDF for document: {}", document.getDocumentId(), e);
            throw new PrintException("Failed to generate PDF", e);
        }
    }

    private void addDocumentContent(Document pdf, Documents document) throws DocumentException {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

        Paragraph title = new Paragraph(document.getTitle(), titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        pdf.add(title);

        pdf.add(new Paragraph("Document Number: " + document.getNumber(), normalFont));
        pdf.add(new Paragraph("Created Date: " + document.getCreatedAt(), normalFont));
        pdf.add(new Paragraph("Status: " + document.getStatus(), normalFont));
        pdf.add(new Paragraph("\n"));
        pdf.add(new Paragraph(document.getContent(), normalFont));
    }

    @Override
    public byte[] generateExcel(List<String> documentIds) throws PrintException {
        log.info("Generating Excel for {} documents", documentIds.size());
        List<Documents> documents = documentIds.stream()
            .map(documentService::findById)
            .collect(Collectors.toList());
            
        byte[] excelBytes = generateExcelContent(documents);
        trackBatchExport(documentIds.size());
        return excelBytes;
    }

    private void trackDocumentOperation(Documents document, String format, TrackingActionType action) {
        trackingService.track(TrackingRequest.builder()
            .actor(userService.getCurrentUser())
            .entityType(TrackingEntityType.DOCUMENT)
            .entityId(document.getDocumentId())
            .action(action)
            .metadata(Map.of(
                "format", format,
                "documentNumber", document.getNumber(),
                "timestamp", LocalDateTime.now().toString()
            ))
            .build());
    }

    private void trackBatchExport(int documentCount) {
        trackingService.track(TrackingRequest.builder()
            .actor(userService.getCurrentUser())
            .entityType(TrackingEntityType.DOCUMENT)
            .entityId("batch-export")
            .action(TrackingActionType.EXPORT)
            .metadata(Map.of(
                "format", "EXCEL",
                "documentCount", String.valueOf(documentCount),
                "timestamp", LocalDateTime.now().toString()
            ))
            .build());
    }

    private byte[] generateExcelContent(List<Documents> documents) {
        try (Workbook workbook = new XSSFWorkbook(); 
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Documents");
            CellStyle headerStyle = createHeaderStyle(workbook);
            
            Row headerRow = sheet.createRow(0);
            String[] columns = {"Number", "Title", "Type", "Status", "Created Date", "Created By"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }
            
            int rowNum = 1;
            for (Documents doc : documents) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(doc.getNumber());
                row.createCell(1).setCellValue(doc.getTitle());
                row.createCell(2).setCellValue(doc.getType().ordinal());
                row.createCell(3).setCellValue(doc.getStatus().toString());
                row.createCell(4).setCellValue(doc.getCreatedAt().toString());
                row.createCell(5).setCellValue(doc.getCreateBy().getUsername());
            }
            
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new PrintException("Failed to generate Excel file", e);
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    @Override
    @Transactional
    public void printDocument(String documentId, String printerName) {
        byte[] pdfBytes = generatePDF(documentId);
        javax.print.PrintService printService = findPrintService(printerName);
        
        if (printService == null) {
            throw new PrintException("Printer not found: " + printerName);
        }
        
        try {
            DocFlavor flavor = DocFlavor.BYTE_ARRAY.PDF;
            Doc doc = new SimpleDoc(pdfBytes, flavor, null);
            PrintRequestAttributeSet attrs = new HashPrintRequestAttributeSet();
            
            DocPrintJob job = printService.createPrintJob();
            job.print(doc, attrs);
            
            savePrintHistory(documentId, userService.getCurrentUser().getUserId(), printerName);
        } catch (PrintException e) {
            throw new PrintException("Failed to print document", e);
        } catch (javax.print.PrintException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> getAvailablePrinters() {
        return Arrays.stream(PrintServiceLookup.lookupPrintServices(null, null))
            .map(javax.print.PrintService::getName)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void savePrintHistory(String documentId, String userId, String printerName) {
        trackingService.track(TrackingRequest.builder()
            .actor(userService.getCurrentUser())
            .entityType(TrackingEntityType.DOCUMENT)
            .entityId(documentId)
            .action(TrackingActionType.PRINT)
            .metadata(Map.of(
                "printer", printerName,
                "userId", userId,
                "timestamp", LocalDateTime.now().toString()
            ))
            .build());
    }

    private javax.print.PrintService findPrintService(String printerName) {
        return Arrays.stream(PrintServiceLookup.lookupPrintServices(null, null))
            .filter(service -> service.getName().equalsIgnoreCase(printerName))
            .findFirst()
            .orElse(null);
    }
} 