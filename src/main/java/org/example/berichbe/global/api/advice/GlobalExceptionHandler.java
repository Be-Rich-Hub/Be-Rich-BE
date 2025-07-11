package org.example.berichbe.global.api.advice;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.example.berichbe.global.api.dto.ApiResponse;
import org.example.berichbe.global.api.exception.ApiException;
import org.example.berichbe.global.api.exception.ValidationException;
import org.example.berichbe.global.api.status.common.CommonErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 커스텀 예외 통합 처리
    @ExceptionHandler(ApiException.class)
    public ApiResponse<?> handleApiException(ApiException ex) {
        log.warn("Handled ApiException: {}", ex.getMessage());
        return ApiResponse.fail(ex.getErrorCode().getCode(), ex.getErrorCode().getMessage());
    }

    // ValidationException 처리 (필드 에러 포함)
    @ExceptionHandler(ValidationException.class)
    public ApiResponse<?> handleValidationException(ValidationException ex) {
        log.warn("ValidationException: {}", ex.getMessage());
        return ApiResponse.fail(ex, ex.getErrorCode(), ex.getFieldErrors());
    }

    // Spring 표준 validation 예외를 ValidationException으로 변환
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<?> handleSpringValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );
        throw new ValidationException(CommonErrorCode.VALIDATION_FAILED, errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResponse<?> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String path = violation.getPropertyPath().toString();
            String field = path.contains(".") ? path.substring(path.lastIndexOf(".") + 1) : path;
            errors.put(field, violation.getMessage());
        });
        throw new ValidationException(CommonErrorCode.VALIDATION_FAILED, errors);
    }

    // 기타 Spring 표준 예외를 커스텀 예외로 변환
    @ExceptionHandler({NoResourceFoundException.class, HttpRequestMethodNotSupportedException.class})
    public ApiResponse<?> handleNotFound(Exception ex) {
        throw new ApiException(CommonErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResponse<?> handleInvalidRequest(HttpMessageNotReadableException ex) {
        throw new ApiException(CommonErrorCode.ILLEGAL_ARGUMENT, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ApiResponse<?> handleMissingParam(MissingServletRequestParameterException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put(ex.getParameterName(), "필수 파라미터입니다.");
        throw new ValidationException(CommonErrorCode.VALIDATION_FAILED, errors);
    }

    // 모든 미처리 예외
    @ExceptionHandler(Exception.class)
    public ApiResponse<?> handleUncaught(Exception ex) {
        log.error("[UNCAUGHT] 서버 내부 오류", ex);
        return ApiResponse.fail(CommonErrorCode.INTERNAL_SERVER_ERROR.getCode(), CommonErrorCode.INTERNAL_SERVER_ERROR.getMessage());
    }
}
