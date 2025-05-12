package org.example.berichbe.domain.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class SignUpRequiredResponseDto {
    private Long providerId;
    private String email;
    private String nickname;
    private String message;
} 