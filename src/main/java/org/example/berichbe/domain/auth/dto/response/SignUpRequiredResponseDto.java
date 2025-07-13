package org.example.berichbe.domain.auth.dto.response;

/**
 * 소셜 로그인 시, DB에 유저 정보가 없어 회원가입이 필요할 때 클라이언트에게 반환하는 DTO
 * @param providerType 소셜 로그인 제공자 (e.g., "KAKAO")
 * @param providerId 제공자로부터 받은 고유 ID
 * @param email 제공자로부터 받은 이메일
 */
public record SignUpRequiredResponseDto(
        String providerType,
        String providerId,
        String email
) {
} 