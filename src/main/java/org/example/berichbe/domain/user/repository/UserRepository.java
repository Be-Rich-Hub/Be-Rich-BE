package org.example.berichbe.domain.user.repository;

import org.example.berichbe.domain.user.entity.User; // 변경된 User 엔티티 경로
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email); // 이메일로 사용자 조회
    boolean existsByEmail(String email); // 이메일 중복 확인
} 