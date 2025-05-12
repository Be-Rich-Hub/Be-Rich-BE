package org.example.berichbe.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.berichbe.domain.auth.dto.request.KakaoLoginRequestDto;
import org.example.berichbe.domain.auth.dto.request.LoginRequestDto;
import org.example.berichbe.domain.auth.dto.request.SignUpRequestDto;
import org.example.berichbe.domain.auth.dto.response.LoginResponseDto;
import org.example.berichbe.domain.auth.dto.response.SignUpRequiredResponseDto;
import org.example.berichbe.domain.auth.service.AuthService;
import org.example.berichbe.domain.auth.service.KakaoApiService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "인증 API", description = "사용자 로그인 및 회원가입 관련 API")
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final KakaoApiService kakaoApiService;

    private ResponseEntity<Map<String, String>> createErrorResponse(HttpStatus status, String error, String message) {
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("error", error);
        responseBody.put("message", message);
        return ResponseEntity.status(status).body(responseBody);
    }

    @Operation(summary = "카카오 로그인/등록", description = "카카오 Access Token을 사용하여 로그인하거나 신규 사용자인 경우 등록 절차를 안내합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공 또는 회원가입 필요 정보 반환",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(oneOf = {LoginResponseDto.class, SignUpRequiredResponseDto.class}))),
            @ApiResponse(responseCode = "400", description = "잘못된 카카오 토큰 또는 사용자 정보 조회 실패"),
            @ApiResponse(responseCode = "401", description = "카카오 인증 실패"),
            @ApiResponse(responseCode = "409", description = "이메일 중복 (카카오 제공 이메일이 이미 시스템에 존재)"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/kakao/login")
    public ResponseEntity<?> kakaoLoginOrRegister(@Valid @RequestBody KakaoLoginRequestDto kakaoLoginRequestDto) {
        try {
            Object result = authService.kakaoLoginOrRegister(kakaoLoginRequestDto);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            log.error("Kakao login error - Invalid argument: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.BAD_REQUEST, "InvalidArgument", e.getMessage());
        } catch (IllegalStateException e) {
            log.error("Kakao login error - Illegal state: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.CONFLICT, "IllegalState", e.getMessage());
        } catch (RuntimeException e) {
            log.error("Kakao login error - RuntimeException: {}", e.getMessage(), e);
            if (e.getMessage() != null && e.getMessage().contains("카카오 API 호출 중 오류")) {
                 return createErrorResponse(HttpStatus.UNAUTHORIZED, "KakaoApiError", "카카오 인증 또는 사용자 정보 조회에 실패했습니다.");
            }
            return createErrorResponse(HttpStatus.BAD_REQUEST, "KakaoProcessingError", e.getMessage());
        } catch (Exception e) {
            log.error("Kakao login error - Unexpected exception: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "ServerError", "카카오 로그인 처리 중 예기치 않은 오류가 발생했습니다.");
        }
    }

    @Operation(summary = "회원가입", description = "서비스 자체 회원가입을 처리합니다. 소셜 로그인 정보가 있다면 함께 처리합니다. (ex.\"KAKAO\")")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "회원가입 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (예: 입력값 유효성 검사 실패, 지원하지 않는 소셜 타입)"),
            @ApiResponse(responseCode = "409", description = "이메일 중복 또는 이미 연동된 카카오 ID"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpRequestDto signUpRequestDto) {
        try {
            LoginResponseDto loginResponseDto = authService.signUp(signUpRequestDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(loginResponseDto);
        } catch (IllegalStateException e) {
            log.error("Sign up error - Illegal state: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.CONFLICT, "IllegalState", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Sign up error - Invalid argument: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.BAD_REQUEST, "InvalidArgument", e.getMessage());
        } catch (Exception e) {
            log.error("Sign up error - Unexpected exception: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "ServerError", "회원가입 처리 중 예기치 않은 오류가 발생했습니다.");
        }
    }

    @Operation(summary = "일반 로그인", description = "이메일과 비밀번호를 사용하여 로그인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (이메일 또는 비밀번호 불일치, 사용자 없음)"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        try {
            LoginResponseDto loginResponse = authService.login(loginRequestDto);
            return ResponseEntity.ok(loginResponse);
        } catch (UsernameNotFoundException e) {
            log.warn("Login attempt failed - User not found: {}", loginRequestDto.getEmail(), e);
            return createErrorResponse(HttpStatus.UNAUTHORIZED, "UserNotFound", "사용자를 찾을 수 없거나 비밀번호가 일치하지 않습니다.");
        } catch (AuthenticationException e) {
            log.warn("Login attempt failed - Authentication error: {}", loginRequestDto.getEmail(), e);
            return createErrorResponse(HttpStatus.UNAUTHORIZED, "AuthenticationError", "사용자를 찾을 수 없거나 비밀번호가 일치하지 않습니다.");
        } catch (Exception e) {
            log.error("Login error - Unexpected exception: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "ServerError", "로그인 처리 중 예기치 않은 오류가 발생했습니다.");
        }
    }

    // ============== 테스트용 엔드포인트 ==============
    @Operation(summary = "[테스트용] 카카오 인가 코드로 Access Token 발급", description = "카카오 인가 코드를 받아 Access Token을 발급하여 반환합니다. (프론트 연동 전 테스트 목적)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Access Token 발급 성공", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "인가 코드 누락 또는 카카오 API 호출 실패"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/kakao/token/test")
    public ResponseEntity<?> getKakaoAccessTokenForTest(@RequestParam("code") String code) {
        if (code == null || code.trim().isEmpty()) {
            return createErrorResponse(HttpStatus.BAD_REQUEST, "MissingCode", "인가 코드가 필요합니다.");
        }
        try {
            String accessToken = kakaoApiService.getAccessTokenFromKakao(code);
            Map<String, String> response = new HashMap<>();
            response.put("kakao_access_token", accessToken);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error getting Kakao access token for test: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.BAD_REQUEST, "KakaoApiError", "카카오 Access Token 발급 중 오류 발생: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error in getKakaoAccessTokenForTest: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "ServerError", "테스트용 카카오 토큰 발급 중 예기치 않은 오류 발생");
        }
    }
} 