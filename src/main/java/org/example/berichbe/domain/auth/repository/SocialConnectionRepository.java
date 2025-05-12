package org.example.berichbe.domain.auth.repository;

import org.example.berichbe.domain.auth.entity.SocialConnection;
import org.example.berichbe.domain.auth.enums.ProviderType;
import org.example.berichbe.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SocialConnectionRepository extends JpaRepository<SocialConnection, Long> {

    // 특정 사용자의 특정 소셜 계정 연동 정보 조회
    Optional<SocialConnection> findByUserAndProvider(User user, ProviderType provider);

    // 특정 소셜 제공자의 특정 providerId로 연동 정보 조회 (가입 여부 확인용)
    Optional<SocialConnection> findByProviderAndProviderId(ProviderType provider, String providerId);

    // 특정 사용자의 모든 소셜 계정 연동 정보 조회 (필요시 사용)
    // List<SocialConnection> findAllByUser(User user);
} 