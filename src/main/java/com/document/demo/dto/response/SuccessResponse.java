package com.document.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SuccessResponse {
    private String message;
    private Object data;

    public SuccessResponse(String message) {
        this.message = message;
        this.data = null;
    }
} 