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
import org.example.berichbe.domain.auth.enums.AuthErrorCode;
import org.example.berichbe.domain.auth.exception.EmailAlreadyExistsException;
import org.example.berichbe.domain.auth.exception.InvalidProviderTypeException;
import org.example.berichbe.domain.auth.exception.KakaoApiFailedException;
import org.example.berichbe.domain.auth.exception.SocialAccountAlreadyLinkedException;
import org.example.berichbe.domain.auth.service.AuthService;
import org.example.berichbe.domain.auth.service.KakaoApiService;
import org.example.berichbe.global.api.exception.ApiException;
import org.example.berichbe.global.api.exception.BadRequestException;
import org.example.berichbe.global.api.status.common.CommonErrorCode;
import org.example.berichbe.global.api.status.common.CommonSuccessCode;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;


import java.util.Map;

@Tag(name = "인증 API", description = "사용자 로그인 및 회원가입 관련 API")
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final KakaoApiService kakaoApiService;

    @Operation(summary = "카카오 로그인/등록", description = "카카오 Access Token을 사용하여 로그인하거나 신규 사용자인 경우 등록 절차를 안내합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공 또는 회원가입 필요 정보 반환",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(oneOf = {LoginResponseDto.class, SignUpRequiredResponseDto.class}))),
            @ApiResponse(responseCode = "400", description = "잘못된 카카오 토큰 또는 사용자 정보 조회 실패"),
            @ApiResponse(responseCode = "409", description = "이메일 중복 (카카오 제공 이메일이 이미 시스템에 존재)"),
            @ApiResponse(responseCode = "500", description = "카카오 API 연동 실패")
    })
    @PostMapping("/kakao/login")
    public org.example.berichbe.global.api.dto.ApiResponse<?> kakaoLoginOrRegister(@Valid @RequestBody KakaoLoginRequestDto kakaoLoginRequestDto) {
        try {
            Object result = authService.kakaoLoginOrRegister(kakaoLoginRequestDto);
            return org.example.berichbe.global.api.dto.ApiResponse.success(CommonSuccessCode.OK, result);
        } catch (KakaoApiFailedException e) {
            log.error("Kakao login error - Kakao API failed: {}", e.getMessage(), e);
            throw new ApiException(AuthErrorCode.KAKAO_API_FAILED, AuthErrorCode.KAKAO_API_FAILED.getStatus());
        } catch (EmailAlreadyExistsException e) {
            log.warn("Kakao login attempt with existing email: {}", e.getMessage(), e);
            throw new ApiException(AuthErrorCode.EMAIL_ALREADY_EXISTS, AuthErrorCode.EMAIL_ALREADY_EXISTS.getStatus());
        } catch (Exception e) {
            log.error("Kakao login error - Unexpected exception: {}", e.getMessage(), e);
            throw new ApiException(CommonErrorCode.INTERNAL_SERVER_ERROR, CommonErrorCode.INTERNAL_SERVER_ERROR.getStatus());
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
    public org.example.berichbe.global.api.dto.ApiResponse<LoginResponseDto> signUp(@Valid @RequestBody SignUpRequestDto signUpRequestDto) {
        try {
            LoginResponseDto loginResponseDto = authService.signUp(signUpRequestDto);
            return org.example.berichbe.global.api.dto.ApiResponse.success(CommonSuccessCode.CREATED, loginResponseDto);
        } catch (EmailAlreadyExistsException e) {
            log.warn("Sign up attempt with existing email: {}", signUpRequestDto.email(), e);
            throw new ApiException(AuthErrorCode.EMAIL_ALREADY_EXISTS, AuthErrorCode.EMAIL_ALREADY_EXISTS.getStatus());
        } catch (InvalidProviderTypeException e) {
            log.warn("Sign up attempt with invalid provider type: {}", signUpRequestDto.providerType(), e);
            throw new ApiException(AuthErrorCode.INVALID_PROVIDER_TYPE, AuthErrorCode.INVALID_PROVIDER_TYPE.getStatus());
        } catch (SocialAccountAlreadyLinkedException e) {
            log.warn("Sign up attempt with already linked social account: Provider={}, ID={}", signUpRequestDto.providerType(), signUpRequestDto.providerId(), e);
            throw new ApiException(AuthErrorCode.SOCIAL_ACCOUNT_ALREADY_LINKED, AuthErrorCode.SOCIAL_ACCOUNT_ALREADY_LINKED.getStatus());
        } catch (BadRequestException e) {
            log.warn("Sign up attempt with bad request: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Sign up error - Unexpected exception: {}", e.getMessage(), e);
            throw new ApiException(CommonErrorCode.INTERNAL_SERVER_ERROR, CommonErrorCode.INTERNAL_SERVER_ERROR.getStatus());
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
    public org.example.berichbe.global.api.dto.ApiResponse<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        try {
            LoginResponseDto loginResponse = authService.login(loginRequestDto);
            return org.example.berichbe.global.api.dto.ApiResponse.success(CommonSuccessCode.OK, loginResponse);
        } catch (AuthenticationException e) {
            log.warn("Login attempt failed for email [{}]: {}", loginRequestDto.email(), e.getMessage());
            throw new ApiException(CommonErrorCode.UNAUTHORIZED, CommonErrorCode.UNAUTHORIZED.getStatus());
        } catch (Exception e) {
            log.error("Login error - Unexpected exception for email [{}]: {}", loginRequestDto.email(), e.getMessage(), e);
            throw new ApiException(CommonErrorCode.INTERNAL_SERVER_ERROR, CommonErrorCode.INTERNAL_SERVER_ERROR.getStatus());
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
    public org.example.berichbe.global.api.dto.ApiResponse<?> getKakaoAccessTokenForTest(@RequestParam("code") String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new ApiException(CommonErrorCode.ILLEGAL_ARGUMENT, CommonErrorCode.ILLEGAL_ARGUMENT.getStatus());
        }
        try {
            String accessToken = kakaoApiService.getAccessTokenFromKakao(code);
            return org.example.berichbe.global.api.dto.ApiResponse.success(CommonSuccessCode.OK, Map.of("kakao_access_token", accessToken));
        } catch (RuntimeException e) {
            log.error("Error getting Kakao access token for test: {}", e.getMessage(), e);
            throw new ApiException(AuthErrorCode.KAKAO_API_FAILED, AuthErrorCode.KAKAO_API_FAILED.getStatus());
        } catch(Exception e) {
            log.error("Error getting Kakao access token for test: {}", e.getMessage(), e);
            throw new ApiException(CommonErrorCode.INTERNAL_SERVER_ERROR, CommonErrorCode.INTERNAL_SERVER_ERROR.getStatus());
        }
    }
} 