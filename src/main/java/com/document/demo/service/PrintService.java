package com.document.demo.service;

import javax.print.PrintException;
import java.util.List;

public interface PrintService {
    byte[] generatePDF(String documentId) throws PrintException;
    byte[] generateExcel(List<String> documentIds) throws PrintException;
    void printDocument(String documentId, String printerName) throws PrintException;
    List<String> getAvailablePrinters();
    void savePrintHistory(String documentId, String userId, String printerName);
} 