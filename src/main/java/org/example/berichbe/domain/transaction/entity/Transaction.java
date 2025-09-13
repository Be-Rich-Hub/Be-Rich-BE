package org.example.berichbe.domain.transaction.entity;


import jakarta.persistence.*;
import lombok.*;
import org.example.berichbe.domain.category.entity.Category;
import org.example.berichbe.domain.member.entity.Member;
import org.example.berichbe.domain.transaction.enums.PaymentMethod;
import org.example.berichbe.domain.transaction.enums.TransactionType;
import org.example.berichbe.global.entity.BaseTimeEntity;

@Table(name = "Transaction")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Transaction extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false, updatable = false, name = "transaction_id")
    private Long id;

    // 다대일
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // FK 컬럼명 : user_id
    private Member member;

    //다대일
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false) // FK 컬럼명 : category_id
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type; // 거래타입

    @Column(nullable = false)
    private int amount; // 가격

    private String memo; // 메모

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method; // 결제 방법

    @Builder
    private Transaction(Member member, Category category, TransactionType type, int amount, String memo, PaymentMethod method) {
        this.member = member;
        this.category = category;
        this.type = type;
        this.amount = amount;
        this.memo = memo;
        this.method = method;
    }

    //== 정적 팩토리 메서드 ==//
    public static Transaction createTransaction(Member member, Category category, TransactionType type, int amount, String memo, PaymentMethod method) {
        return Transaction.builder()
                .member(member)
                .category(category)
                .type(type)
                .amount(amount)
                .memo(memo)
                .method(method)
                .build();
    }
}
