package org.example.berichbe.domain.auth.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.berichbe.domain.auth.enums.ProviderType;
import org.example.berichbe.domain.member.entity.Member;
import org.example.berichbe.global.entity.BaseTimeEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "social_connections", uniqueConstraints = {@UniqueConstraint(columnNames = {"provider", "providerId"})})
@Inheritance(strategy = InheritanceType.JOINED) // 상속 전략: 조인 테이블 사용
@DiscriminatorColumn(name = "provider_type_discriminator") // 어떤 자식 엔티티인지 구분하는 컬럼 (기본값: DTYPE)
public abstract class SocialConnection extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProviderType provider; // 예: KAKAO, APPLE

    @Column(nullable = false, unique = true, length = 255) // provider 마다 고유해야 함 (실제로는 provider + providerId 조합이 unique)
    private String providerId; // 소셜 제공자가 발급한 사용자 고유 ID

    // 자식 클래스에서만 호출할 수 있도록 protected로 변경
    protected SocialConnection(Member member, ProviderType provider, String providerId) {
        this.member = member;
        this.provider = provider;
        this.providerId = providerId;
    }

    //== 연관관계 편의 메서드 ==//
    public void setMember(Member member) {
        this.member = member;
    }
} 