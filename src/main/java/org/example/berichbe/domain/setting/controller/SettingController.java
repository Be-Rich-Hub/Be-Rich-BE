package org.example.berichbe.domain.setting.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.berichbe.domain.setting.dto.BudgetRequestDto;
import org.example.berichbe.domain.setting.service.SettingService;
import org.example.berichbe.domain.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "설정 API", description = "사용자 설정 관련 API (예산 등)")
@Slf4j
@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingController {

    private final SettingService settingService;
    private final UserRepository userRepository;

    private ResponseEntity<Map<String, String>> createErrorResponse(HttpStatus status, String error, String message) {
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("error", error);
        responseBody.put("message", message);
        return ResponseEntity.status(status).body(responseBody);
    }

    @Operation(summary = "예산 설정", description = "로그인한 사용자의 예산을 설정합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "예산 설정 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청 (예: 예산 금액 오류, 사용자 ID 오류)")
    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    @PostMapping("/budget")
    public ResponseEntity<?> setBudget(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody BudgetRequestDto budgetRequestDto
    ) {
        if (userDetails == null) {
            log.warn("Unauthorized budget setting attempt without UserDetails.");
            return createErrorResponse(HttpStatus.UNAUTHORIZED, "Unauthorized", "인증 정보가 없습니다.");
        }

        try {
            org.example.berichbe.domain.user.entity.User currentUser = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("인증된 사용자를 DB에서 찾을 수 없습니다: " + userDetails.getUsername()));

            settingService.setBudget(currentUser.getId(), budgetRequestDto);
            return ResponseEntity.ok().body(Map.of("message", "예산이 성공적으로 설정되었습니다."));
        } catch (UsernameNotFoundException e) {
            log.warn("Budget setting error - User not found from UserDetails: {}", userDetails.getUsername(), e);
            return createErrorResponse(HttpStatus.NOT_FOUND, "UserNotFound", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Budget setting error - Invalid argument: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.BAD_REQUEST, "InvalidArgument", e.getMessage());
        } catch (RuntimeException e) {
             log.error("Budget setting error - RuntimeException: {}", e.getMessage(), e);
             return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "ServerError", "예산 설정 중 오류가 발생했습니다: " + e.getMessage());
        } catch (Exception e) {
            log.error("Budget setting error - Unexpected exception: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "ServerError", "예산 설정 중 예기치 않은 오류가 발생했습니다.");
        }
    }
} 