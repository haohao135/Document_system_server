package com.document.demo.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Date;

@Document
public class Comment {
    @Id
    private String commentId;
    private String comment;
    private String distributionDetailed;
    private LocalDateTime timestamp;
}
