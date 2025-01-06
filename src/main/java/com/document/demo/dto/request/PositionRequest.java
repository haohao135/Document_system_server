package com.document.demo.dto.request;

import com.document.demo.models.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PositionRequest {
    @NotBlank(message = "Position is required")
    private String position;

    @Builder.Default
    private UserRole role = UserRole.USER;
}
