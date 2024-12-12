package com.document.demo.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Document
public class Documents {
    @Id
    private String number;
    private String title;
    private String content;
    private LocalDateTime issueDate;
    private LocalDateTime receivedDate;
    private LocalDateTime sendDate;
    private String type;
    private String status;
    private String attachment;
    private String keywords;
    private String urgencyLevel;
    private LocalDateTime expirationDate;
    private String createBy;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;
    private String logNote;
    private List<String> checkMarkId;
}
