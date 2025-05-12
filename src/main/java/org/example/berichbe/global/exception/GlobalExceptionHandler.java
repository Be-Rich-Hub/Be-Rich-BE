package org.example.berichbe.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice 
public class GlobalExceptionHandler {

    // 공통 에러 응답 생성 메소드
    private ResponseEntity<Map<String, Object>> createErrorResponse(HttpStatus status, String errorCode, String message, Map<String, String> validationErrors) {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", status.value());
        responseBody.put("error", errorCode);
        responseBody.put("message", message);
        if (validationErrors != null && !validationErrors.isEmpty()) {
            responseBody.put("errors", validationErrors);
        }
        return ResponseEntity.status(status).body(responseBody);
    }

    // DTO 유효성 검사 실패 시 (jakarta.validation.Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 기본적으로 400 응답
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.warn("Validation failed: {}", ex.getMessage());
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return createErrorResponse(HttpStatus.BAD_REQUEST, "ValidationFailed", "입력값 유효성 검사에 실패했습니다.", errors);
    }

    // 서비스 로직 등에서 명시적으로 잘못된 인수를 전달했을 때
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 400 응답
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage(), ex);
        return createErrorResponse(HttpStatus.BAD_REQUEST, "IllegalArgument", ex.getMessage(), null);
    }

    // 부적절한 상태에서 메소드가 호출되었을 때 (예: 이미 처리된 요청 재시도)
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT) // 409 응답 (또는 상황에 따라 400)
    public ResponseEntity<Map<String, Object>> handleIllegalStateException(IllegalStateException ex) {
        log.warn("Illegal state: {}", ex.getMessage(), ex);
        return createErrorResponse(HttpStatus.CONFLICT, "IllegalState", ex.getMessage(), null);
    }

    // 위에서 명시적으로 처리하지 않은 모든 예외
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 500 응답
    public ResponseEntity<Map<String, Object>> handleAllUncaughtException(Exception ex) {
        log.error("Unhandled exception occurred: {}", ex.getMessage(), ex);
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "InternalServerError", "서버 내부 오류가 발생했습니다. 관리자에게 문의해주세요.", null);
    }
} 