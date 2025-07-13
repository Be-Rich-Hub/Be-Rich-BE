package org.example.berichbe.domain.member.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.berichbe.domain.auth.dto.request.SignUpRequestDto;
import org.example.berichbe.domain.auth.entity.SocialConnection;
import org.example.berichbe.global.entity.BaseTimeEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "members") // 테이블 이름을 'members'로 변경
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Member extends BaseTimeEntity { 

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String email; // 사용자 이메일 (로그인 ID로 사용 가능)

    @Column(length = 100)
    private String password;

    @Column(nullable = false, length = 30)
    private String name; // 사용자 이름 또는 닉네임

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SocialConnection> socialConnections;

    //== 정적 팩토리 메서드 ==//
    /**
     * 회원가입 정보를 바탕으로 Member 엔티티를 생성합니다.
     * @param dto 회원가입 요청 DTO
     * @param encodedPassword 암호화된 비밀번호
     * @return 생성된 Member 엔티티
     */
    public static Member createMember(SignUpRequestDto dto, String encodedPassword) {
        return Member.builder()
                .email(dto.email())
                .password(encodedPassword)
                .name(dto.name())
                .socialConnections(new ArrayList<>())
                .build();
    }

    //== 연관관계 편의 메서드 ==//
    public void addSocialConnection(SocialConnection socialConnection) {
        this.socialConnections.add(socialConnection);
        if (socialConnection.getMember() != this) {
            socialConnection.setMember(this);
        }
    }

    public void updateName(String name) {
        this.name = name;
    }
} 