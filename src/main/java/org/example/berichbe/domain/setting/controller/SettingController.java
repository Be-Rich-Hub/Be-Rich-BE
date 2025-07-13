package org.example.berichbe.domain.setting.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.berichbe.domain.setting.dto.BudgetRequestDto;
import org.example.berichbe.domain.setting.service.SettingService;
import org.example.berichbe.domain.member.entity.Member;
import org.example.berichbe.domain.member.repository.MemberRepository;
import org.example.berichbe.global.api.exception.ApiException;
import org.example.berichbe.global.api.exception.BadRequestException;
import org.example.berichbe.global.api.exception.NotFoundException;
import org.example.berichbe.global.api.status.common.CommonErrorCode;
import org.example.berichbe.global.api.status.common.CommonSuccessCode;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "설정 API", description = "사용자 설정 관련 API (예산 등)")
@Slf4j
@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingController {

    private final SettingService settingService;
    private final MemberRepository memberRepository;

    @Operation(summary = "예산 설정", description = "로그인한 사용자의 예산을 설정합니다.")
    @ApiResponse(responseCode = "200", description = "예산 설정 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청 (예: 예산은 음수일 수 없습니다)")
    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    @PostMapping("/budget")
    public org.example.berichbe.global.api.dto.ApiResponse<String> setBudget(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody BudgetRequestDto budgetRequestDto
    ) {
        if (userDetails == null) {
            log.warn("Unauthorized budget setting attempt without UserDetails.");
            throw new ApiException(CommonErrorCode.UNAUTHORIZED, CommonErrorCode.UNAUTHORIZED.getStatus());
        }

        try {
            Member currentMember = memberRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new ApiException(CommonErrorCode.USER_NOT_FOUND, CommonErrorCode.USER_NOT_FOUND.getStatus()));

            settingService.setBudget(currentMember.getId(), budgetRequestDto);
            return org.example.berichbe.global.api.dto.ApiResponse.success(CommonSuccessCode.OK, "예산이 성공적으로 설정되었습니다.");
        } catch (NotFoundException e) {
            log.warn("Budget setting error - Resource not found: {}", e.getMessage(), e);
            throw e;
        } catch (BadRequestException e) {
            log.error("Budget setting error - Invalid argument: {}", e.getMessage(), e);
            throw e;
        } catch (RuntimeException e) {
             log.error("Budget setting error - RuntimeException: {}", e.getMessage(), e);
             throw new ApiException(CommonErrorCode.INTERNAL_SERVER_ERROR, CommonErrorCode.INTERNAL_SERVER_ERROR.getStatus());
        }
    }
} 