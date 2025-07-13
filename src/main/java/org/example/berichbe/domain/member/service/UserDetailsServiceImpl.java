package org.example.berichbe.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.example.berichbe.domain.member.entity.Member;
import org.example.berichbe.domain.member.repository.MemberRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("해당 이메일을 가진 사용자를 찾을 수 없습니다: " + email));

        // 여기서는 모든 사용자에게 "ROLE_USER" 권한을 부여합니다.
        // 필요에 따라 User 엔티티에 권한 필드를 추가하고 동적으로 권한을 설정할 수 있습니다.
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

        // 소셜 로그인 등으로 비밀번호가 null인 경우, 빈 문자열로 대체하여 UserDetails 객체 생성
        String password = member.getPassword() != null ? member.getPassword() : "";

        return new User(
                member.getEmail(),
                password,
                authorities
        );
    }
} 