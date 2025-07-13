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
import org.example.berichbe.domain.auth.enums.ProviderType;
import org.example.berichbe.domain.auth.exception.InvalidProviderTypeException;
import org.example.berichbe.domain.auth.exception.EmailAlreadyExistsException;
import org.example.berichbe.domain.auth.exception.KakaoApiFailedException;
import org.example.berichbe.domain.auth.exception.SocialAccountAlreadyLinkedException;
import org.example.berichbe.domain.auth.repository.SocialConnectionRepository;
import org.example.berichbe.domain.member.entity.Member;
import org.example.berichbe.domain.member.repository.MemberRepository;
import org.example.berichbe.global.api.exception.BadRequestException;
import org.example.berichbe.global.api.exception.NotFoundException;
import org.example.berichbe.global.api.status.common.CommonErrorCode;
import org.example.berichbe.global.jwt.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final MemberRepository memberRepository;
    private final SocialConnectionRepository socialConnectionRepository;
    private final KakaoApiService kakaoApiService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    /**
     * 카카오 로그인/회원가입
     * @param kakaoLoginRequestDto 카카오 로그인 정보를 담은 DTO
     * @return 로그인 성공 정보 또는 회원가입 필요 정보
     */
    public Object kakaoLoginOrRegister(KakaoLoginRequestDto kakaoLoginRequestDto) {
        // 1. 카카오 토큰으로 카카오 사용자 정보 조회
        KakaoUserInfoResponseDto kakaoUserInfo = kakaoApiService.getUserInfo(kakaoLoginRequestDto.accessToken());
        if (kakaoUserInfo == null || kakaoUserInfo.id() == null) {
            throw new KakaoApiFailedException();
        }
        
        // 2. DB에서 kakaoProviderId로 사용자 조회
        String kakaoProviderId = String.valueOf(kakaoUserInfo.id());
        Optional<SocialConnection> socialConnectionOpt = socialConnectionRepository.findByProviderAndProviderId(ProviderType.KAKAO, kakaoProviderId);

        if (socialConnectionOpt.isPresent()) {
            // 3-1. 기존 사용자: 로그인 처리 (JWT 발급)
            Member member = socialConnectionOpt.get().getMember();
            log.info("기존 사용자 로그인 (카카오): {}", member.getEmail());
            return createLoginResponse(member);
        } else {
            // 3-2. 신규 사용자: 회원가입 필요 정보 반환
            log.info("신규 사용자 (카카오), 회원가입 필요. Kakao Provider ID: {}", kakaoProviderId);
            String emailFromKakao = kakaoUserInfo.getEmail();

            // 이메일 중복 체크 (카카오 이메일이 이미 다른 계정에 사용 중일 수 있음)
            if (emailFromKakao != null && memberRepository.existsByEmail(emailFromKakao)) {
                log.warn("카카오 로그인 시 이미 등록된 이메일로 가입 시도: {}", emailFromKakao);
                throw new EmailAlreadyExistsException();
            }

            return new SignUpRequiredResponseDto(ProviderType.KAKAO.name(), kakaoProviderId, emailFromKakao);
        }
    }

    /**
     * 서비스 자체 회원가입
     * @param signUpRequestDto 회원가입 정보를 담은 DTO
     * @return LoginResponseDto 로그인 성공 정보 (JWT 포함)
     */
    public LoginResponseDto signUp(SignUpRequestDto signUpRequestDto) {
        if (memberRepository.existsByEmail(signUpRequestDto.email())) {
            log.warn("자체 회원가입 시 이미 등록된 이메일로 가입 시도: {}", signUpRequestDto.email());
            throw new EmailAlreadyExistsException();
        }

        String encodedPassword = null; // 소셜 로그인을 대비해 null로 초기화
        // 소셜 로그인인 경우(providerId가 있는 경우)와 자체 회원가입을 분기
        if (StringUtils.hasText(signUpRequestDto.providerId())) {
            log.info("소셜 회원가입 사용자({})는 비밀번호를 null로 설정합니다.", signUpRequestDto.email());
        } else {
            // 자체 회원가입 시에는 반드시 비밀번호 필요
            if (!StringUtils.hasText(signUpRequestDto.password())) {
                log.warn("자체 회원가입 시도 시 비밀번호 누락: email={}", signUpRequestDto.email());
                throw new BadRequestException(CommonErrorCode.ILLEGAL_ARGUMENT);
            }
            encodedPassword = passwordEncoder.encode(signUpRequestDto.password());
        }


        Member newMember = Member.createMember(signUpRequestDto, encodedPassword);
        memberRepository.save(newMember);
        log.info("신규 사용자 가입 완료: {}", newMember.getEmail());

        // 소셜 연동 정보가 있다면 관련 엔티티 생성 및 연결
        if (StringUtils.hasText(signUpRequestDto.providerType()) && StringUtils.hasText(signUpRequestDto.providerId())) {
            handleSocialConnection(signUpRequestDto, newMember);
        } else if (StringUtils.hasText(signUpRequestDto.providerType()) || StringUtils.hasText(signUpRequestDto.providerId())) {
            throw new BadRequestException(CommonErrorCode.ILLEGAL_ARGUMENT);
        }

        return createLoginResponse(newMember);
    }

    private void handleSocialConnection(SignUpRequestDto signUpRequestDto, Member newMember) {
        ProviderType providerTypeEnum;
        try {
            providerTypeEnum = ProviderType.valueOf(signUpRequestDto.providerType().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("지원하지 않는 ProviderType 입니다: {}", signUpRequestDto.providerType());
            throw new InvalidProviderTypeException();
        }

        String socialProviderId = signUpRequestDto.providerId();
        if (socialConnectionRepository.findByProviderAndProviderId(providerTypeEnum, socialProviderId).isPresent()) {
            log.warn("이미 연동된 소셜 계정입니다: ProviderType: {}, ProviderId: {}", providerTypeEnum, socialProviderId);
            throw new SocialAccountAlreadyLinkedException();
        }
        
        SocialConnection socialConnection;
        if (providerTypeEnum == ProviderType.KAKAO) {
            try {
                Long kakaoProviderIdLong = Long.parseLong(socialProviderId);
                socialConnection = KakaoConnection.builder().member(newMember).kakaoProviderId(kakaoProviderIdLong).build();
            } catch (NumberFormatException e) {
                throw new BadRequestException(CommonErrorCode.ILLEGAL_ARGUMENT);
            }
        } else {
            // 다른 소셜 로그인 추가 시 여기에 분기 처리
            throw new InvalidProviderTypeException();
        }

        newMember.addSocialConnection(socialConnection);
        
    }

    /**
     * 일반 이메일/비밀번호 로그인
     * @param loginRequestDto 로그인 요청 정보 (이메일, 비밀번호)
     * @return LoginResponseDto 로그인 성공 정보 (JWT 포함)
     */
    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        Member member = memberRepository.findByEmail(loginRequestDto.email()).orElseThrow(() -> new NotFoundException(CommonErrorCode.USER_NOT_FOUND));

        return createLoginResponse(member); // 기존의 JWT 발급 및 응답 생성 로직 재활용
    }

    /**
     * Member 엔티티 기반으로 LoginResponseDto 생성 (JWT 발급 포함)
     * @param member 사용자 엔티티
     * @return LoginResponseDto
     */
    private LoginResponseDto createLoginResponse(Member member) {
        // Spring Security Authentication 객체 생성 (UserDetails 기반)
        // 여기서는 간단히 이메일과 권한만으로 Authentication 객체를 생성하여 토큰 발급에 사용
        // UserDetailsServiceImpl의 loadUserByUsername 로직과 유사하게 권한 설정 가능
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        Authentication authentication = new UsernamePasswordAuthenticationToken(member.getEmail(), null, authorities);

        String accessToken = jwtTokenProvider.createAccessToken(authentication);
        // String refreshToken = jwtTokenProvider.generateRefreshToken(authentication); // 리프레시 토큰 기능은 현재 미구현
        boolean budgetSet = member.getBudget() != null;

        return new LoginResponseDto(accessToken, member.getId(), member.getName(), member.getEmail(), budgetSet);
    }
} 