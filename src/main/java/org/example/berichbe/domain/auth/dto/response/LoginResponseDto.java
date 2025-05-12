package org.example.berichbe.domain.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class LoginResponseDto {
    private String accessToken; // 우리 서비스 JWT
    // private String refreshToken; // 추후 리프레시 토큰 추가
    private Long userId;
    private String name;
    private String email;
    private boolean budgetSet;
} 