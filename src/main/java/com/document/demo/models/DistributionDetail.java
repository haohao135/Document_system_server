package com.document.demo.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Document
public class DistributionDetail {
    @Id
    private String distributionDetailId;
    private String attachment;
    private String attachmentFinal;
    private String status;
    private String note;
    private String receivedId;
    private LocalDateTime timestamp;
    private List<String> commentId;
    private List<String> documentsId;
}
