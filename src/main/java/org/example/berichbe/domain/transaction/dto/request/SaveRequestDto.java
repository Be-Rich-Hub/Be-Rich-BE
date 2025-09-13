package org.example.berichbe.domain.transaction.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.example.berichbe.domain.transaction.enums.PaymentMethod;
import org.example.berichbe.domain.transaction.enums.TransactionType;
import org.example.berichbe.global.validation.ValueOfEnum;

public record SaveRequestDto(
        @NotNull(message = "사용자 ID는 필수입니다.")
        Long userId,

        @NotNull(message = "카테고리 ID는 필수입니다.")
        Long categoryId,

        @NotBlank(message = "거래 타입(수입/지출)은 필수입니다.")
        @ValueOfEnum(enumClass = TransactionType.class, message = "거래 타입은 INCOME 또는 EXPENSE 여야 합니다.")
        String type,

        @NotNull(message = "금액은 필수입니다.")
        @Positive(message = "금액은 0보다 커야 합니다.")
        Integer amount,

        String memo,

        @NotBlank(message = "결제 방법(카드/현금/계좌이체)은 필수입니다.")
        @ValueOfEnum(enumClass = PaymentMethod.class, message = "결제 방법은 CARD, CASH, TRANSFER 여야 합니다.")
        String method
) {}

