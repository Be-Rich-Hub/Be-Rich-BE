package org.example.berichbe.domain.member.repository;

import org.example.berichbe.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> { // User -> Member

    boolean existsByEmail(String email);

    Optional<Member> findByEmail(String email); // User -> Member
} 