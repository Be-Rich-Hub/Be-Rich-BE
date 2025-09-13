package org.example.berichbe.domain.transaction.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.berichbe.domain.transaction.dto.request.SaveRequestDto;
import org.example.berichbe.domain.transaction.dto.response.SaveResponseDto;
import org.example.berichbe.domain.transaction.service.TransactionService;
import org.example.berichbe.global.api.dto.ApiResponse;
import org.example.berichbe.global.api.exception.ApiException;
import org.example.berichbe.global.api.exception.NotFoundException;
import org.example.berichbe.global.api.status.common.CommonErrorCode;
import org.example.berichbe.global.api.status.common.CommonSuccessCode;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "가계부 거래 API", description = "가계부 작성 및 저장 관련 API")
@Slf4j
@RestController
@RequestMapping("/api/transaction")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @Operation(summary = "가계부 작성(저장)", description = "가계부를 작성하고 저장합니다.")
    @PostMapping("/save")
    public ApiResponse<SaveResponseDto> save(@Valid @RequestBody SaveRequestDto saveRequestDto) {

        try{
            SaveResponseDto saveResponseDto = transactionService.save(saveRequestDto);
            return ApiResponse.success(CommonSuccessCode.CREATED, saveResponseDto);

        } catch(NotFoundException e) {

            log.warn("데이터를 찾을 수 없어 가계부 저장에 실패했습니다. {}", e.getMessage());
            throw new ApiException(CommonErrorCode.RESOURCE_NOT_FOUND, CommonErrorCode.RESOURCE_NOT_FOUND.getStatus());

        } catch(Exception e) {
            log.error("가계부 저장 중 예상치 못한 오류 발생", e);
            throw new ApiException(CommonErrorCode.INTERNAL_SERVER_ERROR, CommonErrorCode.INTERNAL_SERVER_ERROR.getStatus());
        }
    }
}
