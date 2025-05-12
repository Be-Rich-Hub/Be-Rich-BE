package org.example.berichbe.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt") // application.yml에서 jwt로 시작하는 값들 바인딩
@Getter
@Setter
public class JwtProperties {
    private String secret;
    private long accessTokenValidityInMilliseconds;
    // private long refreshTokenValidityInMilliseconds; // 필요시 추가
} 