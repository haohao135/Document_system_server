package com.document.demo.models.tracking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeLog {
    private Object oldValue;
    private Object newValue;
    private String fieldName;
    private LocalDateTime changeTime;
} 