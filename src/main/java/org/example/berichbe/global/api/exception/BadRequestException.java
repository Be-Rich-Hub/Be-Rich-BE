package org.example.berichbe.global.api.exception;

import org.example.berichbe.global.api.status.ErrorCode;
import org.springframework.http.HttpStatus;

public class BadRequestException extends ApiException {

    public BadRequestException(final ErrorCode errorCode) {
        super(errorCode, HttpStatus.BAD_REQUEST);
    }
}
