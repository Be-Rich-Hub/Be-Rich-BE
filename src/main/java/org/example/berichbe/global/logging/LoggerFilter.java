package org.example.berichbe.global.logging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

@Slf4j
@Component
public class LoggerFilter extends OncePerRequestFilter {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String SWAGGER_URI = "/swagger-ui";
    private static final String SWAGGER_DOCS_URI = "/v3/api-docs";
    private static final String ACTUATOR_URI = "/api/actuator";

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain) throws ServletException, IOException {
        long start = System.currentTimeMillis(); // 요청 시작 시간 측정

        // 응답 바디를 캐싱하기 위해 래핑
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        wrappedResponse.setCharacterEncoding(StandardCharsets.UTF_8.name());

        // 실제 요청 처리 진행
        filterChain.doFilter(request, wrappedResponse);

        long end = System.currentTimeMillis(); // 요청 종료 시간 측정
        long elapsed = end - start; // 처리 시간 계산

        // 응답 정보(status, error 등) 추출
        ResponseInfo responseInfo = getResponseInfo(wrappedResponse);
        // 요청 파라미터 추출
        String params = getParams(request);
        String paramLog = params.isEmpty() ? "" : " | params=[" + params + "]";
        String errorLog = responseInfo.error() == null ? "" : " | error=[" + responseInfo.error() + "]";

        // 주요 요청/응답 정보 로깅
        log.info("[API] {} {} | status={} | time={}ms | ip={}{}{}",
                request.getMethod(),
                request.getRequestURI(),
                responseInfo.statusCode(),
                elapsed,
                getIp(request),
                errorLog,
                paramLog
        );

        // 응답 바디를 실제 클라이언트에 전달
        wrappedResponse.copyBodyToResponse();
    }

    // 응답 바디에서 status, error 등 주요 정보 추출
    private ResponseInfo getResponseInfo(ContentCachingResponseWrapper response) {
        try {
            String responseBody = new String(response.getContentAsByteArray(), response.getCharacterEncoding());
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            int statusCode = jsonNode.has("status") ? jsonNode.get("status").asInt() : response.getStatus();
            String error = jsonNode.has("error") && !jsonNode.get("error").isNull() ? jsonNode.get("error").asText() : null;

            return new ResponseInfo(statusCode, error);
        } catch (Exception e) {
            log.warn("Failed to parse response body", e);
            return new ResponseInfo(response.getStatus(), null);
        }
    }

    // 프록시 환경에서도 실제 클라이언트 IP 추출
    private String getIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        return (xfHeader == null) ? request.getRemoteAddr() : xfHeader.split(",")[0];
    }

    // 요청 파라미터를 문자열로 변환
    private String getParams(HttpServletRequest request) {
        StringBuilder params = new StringBuilder();
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String name = paramNames.nextElement();
            String value = request.getParameter(name);
            params.append(name).append("=").append(value).append(" ");
        }
        return params.toString().trim();
    }

    // Swagger, Actuator 등 불필요한 경로는 로깅 제외
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return isSwaggerRequest(request) || isActuatorRequest(request);
    }

    private boolean isSwaggerRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith(SWAGGER_URI) || uri.startsWith(SWAGGER_DOCS_URI);
    }

    private boolean isActuatorRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith(ACTUATOR_URI);
    }

    // 응답 정보(status, error) 저장용 record
    private record ResponseInfo(int statusCode, String error) {}
}
