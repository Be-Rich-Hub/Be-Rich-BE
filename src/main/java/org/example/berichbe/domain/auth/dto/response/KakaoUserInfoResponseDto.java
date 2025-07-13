package org.example.berichbe.domain.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoUserInfoResponseDto(
    Long id, // 회원번호

    @JsonProperty("kakao_account")
    KakaoAccount kakaoAccount
) {
    public record KakaoAccount(
        String email,
        Profile profile
    ) {
        public record Profile(
            String nickname
            // @JsonProperty("profile_image_url")
            // String profileImageUrl,
            // @JsonProperty("thumbnail_image_url")
            // String thumbnailImageUrl
        ) {}
    }

    // 필요한 Getter 직접 생성 (카카오 API 응답 구조에 따라)
    public String getEmail() {
        return kakaoAccount != null ? kakaoAccount.email() : null;
    }

    public String getNickname() {
        if (kakaoAccount != null && kakaoAccount.profile() != null) {
            return kakaoAccount.profile().nickname();
        }
        return null;
    }
} 