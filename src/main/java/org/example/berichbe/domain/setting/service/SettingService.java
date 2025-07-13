package org.example.berichbe.domain.setting.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.berichbe.domain.setting.dto.BudgetRequestDto;
import org.example.berichbe.domain.member.entity.Member;
import org.example.berichbe.domain.member.repository.MemberRepository;
import org.example.berichbe.global.api.exception.BadRequestException;
import org.example.berichbe.global.api.exception.NotFoundException;
import org.example.berichbe.global.api.status.common.CommonErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SettingService {

    private final MemberRepository memberRepository;

    public void setBudget(Long memberId, BudgetRequestDto budgetRequestDto) {
        if (budgetRequestDto.amount() < 0) {
            throw new BadRequestException(CommonErrorCode.BUDGET_CANNOT_BE_NEGATIVE);
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(CommonErrorCode.USER_NOT_FOUND));

        member.updateBudget(budgetRequestDto.amount());
        log.info("예산 설정 완료. Member ID: {}, 예산: {}", memberId, budgetRequestDto.amount());
    }
} 