package org.example.berichbe.domain.auth.exception;

import org.example.berichbe.domain.auth.enums.AuthErrorCode;
import org.example.berichbe.global.api.exception.ApiException;

public class InvalidProviderTypeException extends ApiException {
    public InvalidProviderTypeException() {
        super(AuthErrorCode.INVALID_PROVIDER_TYPE, AuthErrorCode.INVALID_PROVIDER_TYPE.getStatus());
    }
} 