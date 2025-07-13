package org.example.berichbe.domain.auth.exception;

import org.example.berichbe.domain.auth.enums.AuthErrorCode;
import org.example.berichbe.global.api.exception.ApiException;

public class SocialAccountAlreadyLinkedException extends ApiException {
    public SocialAccountAlreadyLinkedException() {
        super(AuthErrorCode.SOCIAL_ACCOUNT_ALREADY_LINKED, AuthErrorCode.SOCIAL_ACCOUNT_ALREADY_LINKED.getStatus());
    }
} 