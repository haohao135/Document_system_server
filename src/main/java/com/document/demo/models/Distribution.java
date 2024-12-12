package com.document.demo.models;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Document
public class Distribution {
    @Id
    private String distributionId;
    private String list;
    private String status;
    private LocalDateTime timestamp;
    private String documentId;
    private List<String> distributionDetailId;
}
