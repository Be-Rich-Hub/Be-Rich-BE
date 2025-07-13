package org.example.berichbe.global.api.exception;

import org.example.berichbe.global.api.status.ErrorCode;
import org.springframework.http.HttpStatus;

public class NotFoundException extends ApiException {

    public NotFoundException(final ErrorCode errorCode) {
        super(errorCode, HttpStatus.NOT_FOUND);
    }
}
