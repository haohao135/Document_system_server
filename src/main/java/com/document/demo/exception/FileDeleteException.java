package com.document.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class FileDeleteException extends RuntimeException {
    public FileDeleteException(String message) {
        super(message);
    }
} 