package com.document.demo.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupportUpdateRequest {
    String type;
    String title;
    String content;
    String status;
}
