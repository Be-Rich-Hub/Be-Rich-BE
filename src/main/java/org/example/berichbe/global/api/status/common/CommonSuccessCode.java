package org.example.berichbe.global.api.status.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.example.berichbe.global.api.status.SuccessCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonSuccessCode implements SuccessCode {

    OK(HttpStatus.OK, "BERC-200", "요청이 성공했습니다."),
    CREATED(HttpStatus.CREATED, "BERC-201", "요청이 성공했습니다."),
    NO_CONTENT(HttpStatus.NO_CONTENT, "BERC-204", "요청이 성공했습니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}