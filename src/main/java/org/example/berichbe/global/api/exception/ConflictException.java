package org.example.berichbe.global.api.exception;

import org.example.berichbe.global.api.status.ErrorCode;
import org.springframework.http.HttpStatus;

public class ConflictException extends ApiException {

    public ConflictException(final ErrorCode errorCode) {
        super(errorCode, HttpStatus.CONFLICT);
    }
}