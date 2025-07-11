package org.example.berichbe.domain.setting.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.berichbe.domain.setting.dto.BudgetRequestDto;
import org.example.berichbe.domain.user.entity.User;
import org.example.berichbe.domain.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SettingService {

    private final UserRepository userRepository;

    public void setBudget(Long userId, BudgetRequestDto budgetRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));

        user.updateBudget(budgetRequestDto.getAmount());
        log.info("예산 설정 완료. User ID: {}, 예산: {}", userId, budgetRequestDto.getAmount());
    }
} 