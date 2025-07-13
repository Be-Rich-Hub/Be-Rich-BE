package org.example.berichbe.domain.category.entity;


import jakarta.persistence.*;
import lombok.*;

@Table
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, updatable = false, nullable = false, name = "category_id")
    private Long id;

    public static Category create() {
        return Category.builder()
                .build();
    }
}
