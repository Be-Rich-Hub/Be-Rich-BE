package org.example.berichbe.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record KakaoLoginRequestDto(
    @NotBlank(message = "카카오 액세스 토큰은 필수입니다.")
    String accessToken
) {} 