package org.example.berichbe.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.berichbe.domain.user.entity.User;
import org.example.berichbe.domain.user.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("해당 이메일을 가진 사용자를 찾을 수 없습니다: " + email));

        // 여기서는 모든 사용자에게 "ROLE_USER" 권한을 부여합니다.
        // 필요에 따라 User 엔티티에 권한 필드를 추가하고 동적으로 권한을 설정할 수 있습니다.
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(), // User 엔티티의 password 필드 사용
                authorities
        );
    }

    // UserDetails 객체에서 실제 User 엔티티의 ID를 가져오기 위한 헬퍼 메서드 (선택적)
    // JwtTokenProvider에서 User ID를 사용하기보다는, Controller/Service 단에서
    // Authentication 객체로부터 UserDetails를 가져온 후, UserDetails의 username(email)으로
    // DB에서 User 엔티티를 조회하여 ID를 얻는 것이 일반적입니다.
    // 아래 메서드는 예시이며, UserDetails가 CustomUserDetails일 경우에 유용합니다.
    /*
    public Long getUserId(UserDetails userDetails) {
        if (userDetails instanceof CustomUserDetails) { // CustomUserDetails를 사용한다고 가정
            return ((CustomUserDetails) userDetails).getId();
        }
        // 또는 email로 DB에서 조회
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        return user != null ? user.getId() : null;
    }
    */
} 