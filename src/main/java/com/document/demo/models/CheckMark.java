package com.document.demo.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class CheckMark {
    @Id
    private String checkMarkId;
    private String name;
}
