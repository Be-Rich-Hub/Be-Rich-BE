package org.example.berichbe.domain.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString // 디버깅용
public class KakaoUserInfoResponseDto {

    private Long id; // 회원번호

    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;

    @Getter
    @NoArgsConstructor
    @ToString
    public static class KakaoAccount {
        private String email;
        private Profile profile;

        @Getter
        @NoArgsConstructor
        @ToString
        public static class Profile {
            private String nickname;
            // @JsonProperty("profile_image_url")
            // private String profileImageUrl;
            // @JsonProperty("thumbnail_image_url")
            // private String thumbnailImageUrl;
        }
    }

    // 필요한 Getter 직접 생성 (카카오 API 응답 구조에 따라)
    public String getEmail() {
        return kakaoAccount != null ? kakaoAccount.getEmail() : null;
    }

    public String getNickname() {
        if (kakaoAccount != null && kakaoAccount.getProfile() != null) {
            return kakaoAccount.getProfile().getNickname();
        }
        return null;
    }
} 