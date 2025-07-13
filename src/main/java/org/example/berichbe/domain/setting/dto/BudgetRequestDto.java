package org.example.berichbe.domain.setting.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record BudgetRequestDto(
    @NotNull(message = "예산은 필수입니다.")
    @Min(value = 1, message = "예산은 0보다 커야 합니다.")
    Long amount
) {} 