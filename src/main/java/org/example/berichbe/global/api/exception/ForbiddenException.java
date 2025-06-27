package org.example.berichbe.global.api.exception;

import org.example.berichbe.global.api.status.ErrorCode;
import org.springframework.http.HttpStatus;

public class ForbiddenException extends ApiException {
    public ForbiddenException(final ErrorCode errorCode) {
        super(errorCode, HttpStatus.FORBIDDEN);
    }
}
