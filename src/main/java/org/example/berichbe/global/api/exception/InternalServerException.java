package org.example.berichbe.global.api.exception;

import org.example.berichbe.global.api.status.ErrorCode;
import org.springframework.http.HttpStatus;

public class InternalServerException extends ApiException {
    
    public InternalServerException(ErrorCode errorCode) {
        super(errorCode, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    public InternalServerException(ErrorCode errorCode, String message) {
        super(errorCode, HttpStatus.INTERNAL_SERVER_ERROR);
    }
} 