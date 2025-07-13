package org.example.berichbe.domain.transactional.entity;


import jakarta.persistence.*;
import lombok.*;
import org.example.berichbe.global.entity.BaseTimeEntity;

@Table(name = "Transactionals")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Transactional extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false, updatable = false, name = "transactional_id")
    private Long id;

    public static Transactional create() {
        return Transactional.builder()
                .build();
    }
}
