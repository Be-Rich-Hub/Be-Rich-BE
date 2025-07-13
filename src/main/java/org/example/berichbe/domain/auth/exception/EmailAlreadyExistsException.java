package org.example.berichbe.domain.auth.exception;

import org.example.berichbe.domain.auth.enums.AuthErrorCode;
import org.example.berichbe.global.api.exception.ApiException;

public class EmailAlreadyExistsException extends ApiException {
    public EmailAlreadyExistsException() {
        super(AuthErrorCode.EMAIL_ALREADY_EXISTS, AuthErrorCode.EMAIL_ALREADY_EXISTS.getStatus());
    }
} 