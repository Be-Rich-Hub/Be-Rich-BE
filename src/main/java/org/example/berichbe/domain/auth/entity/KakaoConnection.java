package org.example.berichbe.domain.auth.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.berichbe.domain.auth.enums.ProviderType;
import org.example.berichbe.domain.member.entity.Member;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "kakao_connections") // 카카오 전용 테이블명 유지 (JOINED 전략 사용 시)
@DiscriminatorValue("KAKAO") // SocialConnection의 provider_type_discriminator 컬럼에 저장될 값
public class KakaoConnection extends SocialConnection {

    // KakaoConnection에만 특화된 필드가 있다면 여기에 추가합니다.
    // 예: 카카오 프로필 이미지 URL, 카카오 닉네임 등 (Member 엔티티와 중복될 수 있으므로 신중히 결정)
    // 현재는 특별한 필드가 없으므로 비워둡니다.

    @Builder
    public KakaoConnection(Member member, Long kakaoProviderId) { // 카카오 ID는 Long 타입으로 받음
        super(member, ProviderType.KAKAO, String.valueOf(kakaoProviderId)); // 부모 생성자 호출, providerId는 String으로 변환
    }

    // SocialConnection 에서 id, member, provider, providerId 필드를 상속받으므로 중복 선언 불필요.
    // 기존의 kakaoId 필드는 SocialConnection의 providerId로 대체됨.
    // 기존의 member 필드 및 연관관계는 SocialConnection 에서 관리.
    // 기존의 setMember 편의 메서드도 SocialConnection에서 관리하거나, 필요시 Member 엔티티의 편의 메서드 수정.
} 