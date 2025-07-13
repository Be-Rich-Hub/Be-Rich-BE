package org.example.berichbe.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpRequestDto(
    @NotBlank(message = "이름은 필수입니다.")
    @Size(min = 2, max = 30, message = "이름은 2자 이상 30자 이하로 입력해주세요.")
    String name,

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    @Size(max = 50, message = "이메일은 50자를 초과할 수 없습니다.")
    String email,

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 4, message = "비밀번호는 최소 4자 이상이어야 합니다.") // 비밀번호 정책 추가 가능 (예: 영문, 숫자, 특수문자 조합)
    String password,

    // 소셜 프로바이더로부터 받은 사용자 고유 ID (String 타입으로 변경)
    String providerId, // Long에서 String으로 변경

    // 어떤 소셜 프로바이더인지 구분하는 타입 (예: "KAKAO", "NAVER")
    String providerType
) {} 