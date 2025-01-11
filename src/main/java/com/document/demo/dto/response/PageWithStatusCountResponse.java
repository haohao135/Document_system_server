package com.document.demo.dto.response;

import com.document.demo.models.enums.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageWithStatusCountResponse<T> {
    private List<T> content;
    private int number;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;
    private boolean first;
    private Map<DocumentStatus, Long> statusCounts;

    public static <T> PageWithStatusCountResponse<T> from(Page<T> page, Map<DocumentStatus, Long> statusCounts) {
        return PageWithStatusCountResponse.<T>builder()
            .content(page.getContent())
            .number(page.getNumber())
            .pageSize(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .last(page.isLast())
            .first(page.isFirst())
            .statusCounts(statusCounts)
            .build();
    }
} 