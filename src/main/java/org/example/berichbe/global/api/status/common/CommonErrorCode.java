package org.example.berichbe.global.api.status.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.example.berichbe.global.api.status.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {

    // 400 Bad Request
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "BERC-400", "입력값 유효성 검사에 실패했습니다."),
    ILLEGAL_ARGUMENT(HttpStatus.BAD_REQUEST, "BERC-400", "잘못된 인수가 전달되었습니다."),
    
    // 401 Unauthorized
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "BERC-401", "인증이 필요합니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "BERC-401", "유효하지 않은 토큰입니다."),
    
    // 403 Forbidden
    FORBIDDEN(HttpStatus.FORBIDDEN, "BERC-403", "접근 권한이 없습니다."),
    
    // 404 Not Found
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "BERC-404", "요청한 리소스를 찾을 수 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "BERC-404", "사용자를 찾을 수 없습니다."),
    
    // 409 Conflict
    ILLEGAL_STATE(HttpStatus.CONFLICT, "BERC-409", "요청을 처리할 수 없는 상태입니다."),
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "BERC-409", "이미 존재하는 리소스입니다."),
    
    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "BERC-500", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
} 