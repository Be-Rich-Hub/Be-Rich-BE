package org.example.berichbe.domain.category.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.berichbe.global.api.status.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CategoryErrorCode implements ErrorCode {

    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CATEGORY404_1", "해당 카테고리를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
