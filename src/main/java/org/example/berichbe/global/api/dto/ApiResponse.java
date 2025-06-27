package org.example.berichbe.global.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.example.berichbe.global.api.exception.ApiException;
import org.example.berichbe.global.api.status.ErrorCode;
import org.example.berichbe.global.api.status.SuccessCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {

    private final int status;
    private final String code;
    private final String message;
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private T data;

    public static <T> ApiResponse<T> success(final SuccessCode success) {
        return new ApiResponse<>(success.getStatus().value(), success.getCode(), success.getMessage());
    }

    public static <T> ApiResponse<T> success(final SuccessCode success, final T data) {
        return new ApiResponse<>(success.getStatus().value(), success.getCode(), success.getMessage(), data);
    }

    public static <T> ApiResponse<T> fail(final ApiException ex, final ErrorCode error) {
        return new ApiResponse<>(ex.getStatus().value(), error.getCode(), error.getMessage());
    }

    public static <T> ApiResponse<T> fail(final ApiException ex, final ErrorCode error, final T data) {
        return new ApiResponse<>(ex.getStatus().value(), error.getCode(), error.getMessage(), data);
    }

    public static <T> ApiResponse<T> fail(final String code, final String message) {
        return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), code, message);
    }
}