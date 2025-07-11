package org.example.berichbe.global.api.exception;

import lombok.Getter;
import org.example.berichbe.global.api.status.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final ErrorCode errorCode;

    public ApiException(final ErrorCode errorCode, final HttpStatus status) {
        super(errorCode.getMessage());
        this.status = status;
        this.errorCode = errorCode;
    }
}
