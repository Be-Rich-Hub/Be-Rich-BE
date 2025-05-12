package org.example.berichbe.domain.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.berichbe.domain.auth.dto.request.KakaoLoginRequestDto;
import org.example.berichbe.domain.auth.dto.request.LoginRequestDto;
import org.example.berichbe.domain.auth.dto.request.SignUpRequestDto;
import org.example.berichbe.domain.auth.dto.response.KakaoUserInfoResponseDto;
import org.example.berichbe.domain.auth.dto.response.LoginResponseDto;
import org.example.berichbe.domain.auth.dto.response.SignUpRequiredResponseDto;
import org.example.berichbe.domain.auth.entity.KakaoConnection;
import org.example.berichbe.domain.auth.entity.SocialConnection;
import org.example.berichbe.domain.auth.repository.SocialConnectionRepository;
import org.example.berichbe.domain.auth.enums.ProviderType;
import org.example.berichbe.domain.user.entity.User;
import org.example.berichbe.domain.user.repository.UserRepository;
import org.example.berichbe.global.jwt.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final SocialConnectionRepository socialConnectionRepository;
    private final KakaoApiService kakaoApiService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    /**
     * 카카오 Access Token을 이용한 로그인/회원가입 분기 처리
     * @param kakaoLoginRequestDto 카카오 Access Token을 담은 DTO
     * @return 로그인 성공 시 LoginResponseDto, 회원가입 필요 시 SignUpRequiredResponseDto
     */
    public Object kakaoLoginOrRegister(KakaoLoginRequestDto kakaoLoginRequestDto) {
        // 1. 카카오 토큰으로 카카오 사용자 정보 조회
        KakaoUserInfoResponseDto kakaoUserInfo = kakaoApiService.getUserInfo(kakaoLoginRequestDto.getAccessToken());
        if (kakaoUserInfo == null || kakaoUserInfo.getId() == null) {
            // TODO: 커스텀 예외 처리 (예: KakaoApiFailedException)
            throw new RuntimeException("카카오 사용자 정보를 가져오는데 실패했습니다.");
        }

        Long kakaoProviderIdLong = kakaoUserInfo.getId();
        String kakaoProviderId = String.valueOf(kakaoProviderIdLong);

        // 2. DB에서 kakaoProviderId로 사용자 조회
        Optional<SocialConnection> socialConnectionOpt = socialConnectionRepository.findByProviderAndProviderId(ProviderType.KAKAO, kakaoProviderId);

        if (socialConnectionOpt.isPresent()) {
            // 3-1. 기존 사용자: 로그인 처리 (JWT 발급)
            User user = socialConnectionOpt.get().getUser();
            log.info("기존 사용자 로그인 (카카오): {}", user.getEmail());
            return createLoginResponse(user);
        } else {
            // 3-2. 신규 사용자: 회원가입 필요 정보 반환
            log.info("신규 사용자 (카카오), 회원가입 필요. Kakao Provider ID: {}", kakaoProviderId);
            String emailFromKakao = kakaoUserInfo.getEmail();
            String nicknameFromKakao = kakaoUserInfo.getNickname();

            // 이메일 중복 체크 (카카오 이메일이 이미 다른 계정에 사용 중일 수 있음)
            if (emailFromKakao != null && userRepository.existsByEmail(emailFromKakao)) {
                // TODO: 이메일 중복 시 어떻게 처리할지 정책 필요. 여기서는 예외 발생.
                // 혹은, SignUpRequiredResponseDto에 해당 정보 포함하여 프론트에서 처리 유도
                log.warn("카카오에서 받은 이메일 {}이 이미 시스템에 존재합니다. Kakao Provider ID: {}", emailFromKakao, kakaoProviderId);
                throw new IllegalStateException("이미 사용 중인 이메일입니다. 다른 이메일로 가입해주세요. (카카오 계정의 이메일이 이미 등록되어 있습니다.)");
            }

            return new SignUpRequiredResponseDto(
                    kakaoProviderIdLong,
                    emailFromKakao,
                    nicknameFromKakao,
                    "카카오 계정과 연동된 정보가 없습니다. 회원가입을 진행해주세요."
            );
        }
    }

    /**
     * 서비스 자체 회원가입
     * @param signUpRequestDto 회원가입 정보를 담은 DTO
     * @return LoginResponseDto 로그인 성공 정보 (JWT 포함)
     */
    public LoginResponseDto signUp(SignUpRequestDto signUpRequestDto) {
        if (userRepository.existsByEmail(signUpRequestDto.getEmail())) {
            // TODO: 커스텀 예외 (예: EmailAlreadyExistsException)
            throw new IllegalStateException("이미 사용 중인 이메일입니다.");
        }

        // User 엔티티 생성 및 저장
        User newUser = User.builder()
                .email(signUpRequestDto.getEmail())
                .password(passwordEncoder.encode(signUpRequestDto.getPassword())) // 비밀번호 암호화
                .name(signUpRequestDto.getName())
                .build();
        userRepository.save(newUser);
        log.info("신규 사용자 가입 완료: {}", newUser.getEmail());

        // 소셜 연동 정보가 있다면 관련 엔티티 생성 및 연결
        if (StringUtils.hasText(signUpRequestDto.getProviderType()) && StringUtils.hasText(signUpRequestDto.getProviderId())) {
            ProviderType providerTypeEnum;
            try {
                providerTypeEnum = ProviderType.valueOf(signUpRequestDto.getProviderType().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("지원하지 않는 ProviderType 입니다: {}", signUpRequestDto.getProviderType());
                throw new IllegalArgumentException("지원하지 않는 소셜 로그인 타입입니다: " + signUpRequestDto.getProviderType());
            }

            String socialProviderId = signUpRequestDto.getProviderId();

            if (socialConnectionRepository.findByProviderAndProviderId(providerTypeEnum, socialProviderId).isPresent()) {
                log.warn("이미 연동된 {} Provider ID로 회원가입 시도: {}", providerTypeEnum, socialProviderId);
                throw new IllegalStateException("이미 다른 계정에 연동된 소셜 계정입니다.");
            }

            SocialConnection socialConnection = null;
            switch (providerTypeEnum) {
                case KAKAO:
                    // KakaoConnection의 생성자는 Long 타입의 providerId를 기대하므로 변환 시도
                    try {
                        Long kakaoProviderIdLong = Long.parseLong(socialProviderId);
                        socialConnection = KakaoConnection.builder()
                                .user(newUser)
                                .kakaoProviderId(kakaoProviderIdLong)
                                .build();
                        log.info("카카오 연동 정보 생성. Kakao Provider ID: {}", socialProviderId);
                    } catch (NumberFormatException e) {
                        log.error("카카오 Provider ID가 유효한 Long 타입이 아닙니다: {}", socialProviderId);
                        throw new IllegalArgumentException("카카오 Provider ID는 숫자여야 합니다.");
                    }
                    break;
                // case NAVER: // 추후 네이버 로그인 추가 시
                //     socialConnection = NaverConnection.builder()...build();
                //     log.info("네이버 연동 정보 생성. Naver Provider ID: {}", socialProviderId);
                //     break;
                default:
                    log.warn("지원하지 않는 ProviderType에 대한 SocialConnection 생성 시도: {}", providerTypeEnum);
                    // 이전에 ProviderType.valueOf 에서 걸러지지만, 방어적으로 처리
                    throw new IllegalArgumentException("지원하지 않는 소셜 로그인 타입입니다: " + providerTypeEnum);
            }

            if (socialConnection != null) {
                newUser.addSocialConnection(socialConnection);
                userRepository.save(newUser); // User의 socialConnections 변경사항을 DB에 반영
            }
        } else if (StringUtils.hasText(signUpRequestDto.getProviderType()) || StringUtils.hasText(signUpRequestDto.getProviderId())) {
            // providerType 또는 providerId 중 하나만 제공된 경우
            log.warn("소셜 연동 정보가 올바르지 않습니다. providerType: {}, providerId: {}", signUpRequestDto.getProviderType(), signUpRequestDto.getProviderId());
            throw new IllegalArgumentException("소셜 연동을 위해서는 providerType과 providerId가 모두 필요합니다.");
        }

        return createLoginResponse(newUser);
    }

    /**
     * 일반 이메일/비밀번호 로그인
     * @param loginRequestDto 로그인 요청 정보 (이메일, 비밀번호)
     * @return LoginResponseDto 로그인 성공 정보 (JWT 포함)
     */
    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        // 1. Spring Security를 사용하여 사용자 인증 시도
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.getEmail(),
                        loginRequestDto.getPassword()
                )
        );

        // 2. 인증 성공 시 SecurityContext에 Authentication 객체 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. 인증된 사용자 정보로 User 엔티티 조회
        //    UserDetailsServiceImpl에서 반환된 UserDetails의 username은 email임
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("인증된 사용자를 DB에서 찾을 수 없습니다: " + authentication.getName()));

        // 4. JWT 토큰 생성 및 LoginResponseDto 반환
        return createLoginResponse(user); // 기존의 JWT 발급 및 응답 생성 로직 재활용
    }

    /**
     * User 엔티티 기반으로 LoginResponseDto 생성 (JWT 발급 포함)
     * @param user 사용자 엔티티
     * @return LoginResponseDto
     */
    private LoginResponseDto createLoginResponse(User user) {
        // Spring Security Authentication 객체 생성 (UserDetails 기반)
        // 여기서는 간단히 이메일과 권한만으로 Authentication 객체를 생성하여 토큰 발급에 사용
        // UserDetailsServiceImpl의 loadUserByUsername 로직과 유사하게 권한 설정 가능
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getEmail(), null, authorities);

        String accessToken = jwtTokenProvider.createAccessToken(authentication);
        boolean budgetSet = user.getBudget() != null;

        return new LoginResponseDto(accessToken, user.getId(), user.getName(), user.getEmail(), budgetSet);
    }
} 