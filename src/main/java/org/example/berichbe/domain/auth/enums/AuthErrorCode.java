package org.example.berichbe.domain.auth.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.berichbe.global.api.status.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    // 400 Bad Request
    INVALID_KAKAO_TOKEN(HttpStatus.BAD_REQUEST, "AUTH400_1", "유효하지 않은 카카오 토큰입니다."),
    KAKAO_USER_INFO_FAILED(HttpStatus.BAD_REQUEST, "AUTH400_2", "카카오 사용자 정보를 가져오는데 실패했습니다."),
    INVALID_PROVIDER_TYPE(HttpStatus.BAD_REQUEST, "AUTH400_3", "지원하지 않는 소셜 로그인 타입입니다."),

    // 401 Unauthorized
    KAKAO_AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "AUTH401_1", "카카오 인증에 실패했습니다."),

    // 409 Conflict
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "AUTH409_1", "이미 등록된 이메일입니다."),
    SOCIAL_ACCOUNT_ALREADY_LINKED(HttpStatus.CONFLICT, "AUTH409_2", "이미 연동된 소셜 계정입니다."),

    // 500 Internal Server Error
    KAKAO_API_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH500_1", "카카오 API 연동에 실패했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

} 