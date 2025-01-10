package com.document.demo.models.tracking;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class ChangeLog {
    private Object oldValue;
    private Object newValue;
    private String fieldName;

    @Builder.Default
    private LocalDateTime changeTime = LocalDateTime.now();
} 