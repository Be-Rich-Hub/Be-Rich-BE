package org.example.berichbe.domain.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.berichbe.domain.auth.dto.response.KakaoUserInfoResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoApiService {

    private final RestTemplate restTemplate;

    @Value("${spring.kakao.client-id}")
    private String kakaoClientId;

    @Value("${spring.kakao.redirect-uri}")
    private String kakaoRedirectUri;

    private static final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";
    private static final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";


    /**
     * 카카오 Access Token으로 사용자 정보 조회
     * @param accessToken 카카오에서 발급받은 Access Token
     * @return KakaoUserInfoResponseDto 사용자 정보
     */
    public KakaoUserInfoResponseDto getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken); // "Authorization: Bearer {ACCESS_TOKEN}"
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED); // Content-type 지정

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<KakaoUserInfoResponseDto> response = restTemplate.exchange(
                    KAKAO_USER_INFO_URL,
                    HttpMethod.GET,
                    requestEntity,
                    KakaoUserInfoResponseDto.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("카카오 사용자 정보 조회 성공: {}", response.getBody());
                return response.getBody();
            } else {
                log.error("카카오 사용자 정보 조회 실패: 응답 코드 {}", response.getStatusCode());
                // TODO: 적절한 예외 처리 또는 응답
                throw new RuntimeException("카카오 사용자 정보 조회에 실패했습니다. 응답 코드: " + response.getStatusCode());
            }
        } catch (HttpClientErrorException e) {
            log.error("카카오 API 호출 중 에러 발생 (getUserInfo): {}, 응답 바디: {}", e.getStatusCode(), e.getResponseBodyAsString());
            // TODO: 토큰 만료 등의 경우에 대한 구체적인 예외 처리
            throw new RuntimeException("카카오 API 호출 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 인가 코드로 카카오 Access Token 발급 (백엔드 테스트용)
     * @param code 카카오 인증 서버에서 발급받은 인가 코드
     * @return String 카카오 Access Token
     */
    @SuppressWarnings("rawtypes") // Map 타입 경고 무시
    public String getAccessTokenFromKakao(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoClientId);
        params.add("redirect_uri", kakaoRedirectUri);
        params.add("code", code);
        // client_secret은 필수가 아님 (보안 강화를 위해 사용하는 경우 추가)

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(KAKAO_TOKEN_URL, requestEntity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String accessToken = (String) response.getBody().get("access_token");
                log.info("카카오 Access Token 발급 성공");
                // String refreshToken = (String) response.getBody().get("refresh_token"); // 필요시 리프레시 토큰 저장 및 관리
                return accessToken;
            } else {
                log.error("카카오 Access Token 발급 실패: 응답 코드 {}", response.getStatusCode());
                throw new RuntimeException("카카오 Access Token 발급에 실패했습니다. 응답 코드: " + response.getStatusCode());
            }
        } catch (HttpClientErrorException e) {
            log.error("카카오 API 호출 중 에러 발생 (getAccessTokenFromKakao): {}, 응답 바디: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("카카오 API 호출 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
} 