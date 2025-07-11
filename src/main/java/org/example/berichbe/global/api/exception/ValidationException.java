package org.example.berichbe.global.api.exception;

import lombok.Getter;

import org.example.berichbe.global.api.status.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
public class ValidationException extends ApiException {
    
    private final Map<String, String> fieldErrors;
    
    public ValidationException(ErrorCode errorCode, Map<String, String> fieldErrors) {
        super(errorCode, HttpStatus.BAD_REQUEST);
        this.fieldErrors = fieldErrors;
    }

} 