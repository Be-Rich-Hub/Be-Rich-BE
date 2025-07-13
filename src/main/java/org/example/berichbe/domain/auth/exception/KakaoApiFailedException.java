package org.example.berichbe.domain.auth.exception;

import org.example.berichbe.domain.auth.enums.AuthErrorCode;
import org.example.berichbe.global.api.exception.ApiException;

public class KakaoApiFailedException extends ApiException {
    public KakaoApiFailedException() {
        super(AuthErrorCode.KAKAO_API_FAILED, AuthErrorCode.KAKAO_API_FAILED.getStatus());
    }
} 