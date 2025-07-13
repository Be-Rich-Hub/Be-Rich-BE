package org.example.berichbe.domain.auth.dto.response;

public record LoginResponseDto(
    String accessToken, // 우리 서비스 JWT
    // String refreshToken, // 추후 리프레시 토큰 추가
    Long userId,
    String name,
    String email,
    boolean budgetSet
) {} 