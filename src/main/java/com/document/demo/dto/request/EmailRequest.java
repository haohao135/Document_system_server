package com.document.demo.dto.request;

import lombok.*;

import java.util.Map;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class EmailRequest {
    private String to;
    private String subject;
    private String nameOfSystem;
}
