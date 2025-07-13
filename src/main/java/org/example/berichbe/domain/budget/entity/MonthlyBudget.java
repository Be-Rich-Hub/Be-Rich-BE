package org.example.berichbe.domain.budget.entity;


import jakarta.persistence.*;
import lombok.*;
import org.example.berichbe.global.entity.BaseTimeEntity;

@Table(name = "monthly_budgets")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class MonthlyBudget extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, updatable = false, nullable = false, name = "monthly_budget_id")
    private Long id;

    public static MonthlyBudget create() {
        return MonthlyBudget.builder()
                .build();
    }
}
