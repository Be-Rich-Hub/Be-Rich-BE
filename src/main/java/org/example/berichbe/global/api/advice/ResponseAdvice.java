package org.example.berichbe.global.api.advice;

import org.example.berichbe.global.api.dto.ApiResponse;
import org.example.berichbe.global.api.status.common.CommonSuccessCode;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice(basePackages = "org.example.berichbe")
public class ResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // ApiResponse가 아닌 모든 응답을 처리
        return !ApiResponse.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        // null이거나 이미 ApiResponse인 경우 그대로 반환
        if (body == null || body instanceof ApiResponse<?>) {
            return body;
        }
        
        // 성공 응답으로 래핑
        return ApiResponse.success(CommonSuccessCode.OK, body);
    }
}