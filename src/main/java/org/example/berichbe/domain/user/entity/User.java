package org.example.berichbe.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.berichbe.domain.auth.entity.SocialConnection;
import org.example.berichbe.global.entity.BaseTimeEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users") // 데이터베이스 테이블 이름을 명시적으로 "users"로 지정 (USER는 예약어인 경우가 많음)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String email; // 사용자 이메일 (로그인 ID로 사용 가능)

    @Column(nullable = false, length = 100)
    private String password; // 자체 로그인 시 사용할 비밀번호 (BCrypt 등으로 해싱 필요)

    @Column(nullable = false, length = 30)
    private String name; // 사용자 이름 또는 닉네임

    @Column
    private Long budget;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SocialConnection> socialConnections = new ArrayList<>();

    @Builder
    public User(String email, String password, String name, Long budget) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.budget = budget;
    }

    public void addSocialConnection(SocialConnection socialConnection) {
        this.socialConnections.add(socialConnection);
        if (socialConnection.getUser() != this) {
            socialConnection.setUser(this);
        }
    }

    public void updateBudget(Long budget) {
        this.budget = budget;
    }

    public void updateName(String name) {
        this.name = name;
    }
} 