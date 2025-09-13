package org.example.berichbe.domain.transaction.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.berichbe.domain.category.entity.Category;
import org.example.berichbe.domain.category.enums.CategoryErrorCode;
import org.example.berichbe.domain.category.repository.CategoryRepository;
import org.example.berichbe.domain.member.entity.Member;
import org.example.berichbe.domain.member.repository.MemberRepository;
import org.example.berichbe.domain.transaction.dto.request.SaveRequestDto;
import org.example.berichbe.domain.transaction.dto.response.SaveResponseDto;
import org.example.berichbe.domain.transaction.entity.Transaction;
import org.example.berichbe.domain.transaction.enums.PaymentMethod;
import org.example.berichbe.domain.transaction.enums.TransactionType;
import org.example.berichbe.domain.transaction.repository.TransactionRepository;
import org.example.berichbe.global.api.exception.NotFoundException;
import org.example.berichbe.global.api.status.common.CommonErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public SaveResponseDto save(SaveRequestDto saveRequestDto) {

        log.info("가계부 내역 저장 작업 시작. userId: {}", saveRequestDto.userId());

        Member member = memberRepository.findById(saveRequestDto.userId())
                .orElseThrow(() -> new NotFoundException(CommonErrorCode.USER_NOT_FOUND));

        Category category = categoryRepository.findById(saveRequestDto.categoryId())
                .orElseThrow(() -> new NotFoundException(CategoryErrorCode.CATEGORY_NOT_FOUND));


        TransactionType transactionType = TransactionType.valueOf(saveRequestDto.type().toUpperCase());
        PaymentMethod paymentMethod = PaymentMethod.valueOf(saveRequestDto.method().toUpperCase());

        Transaction transaction = Transaction.createTransaction(
                member,
                category,
                transactionType,
                saveRequestDto.amount(),
                saveRequestDto.memo(),
                paymentMethod
        );

        Transaction savedTransaction = transactionRepository.save(transaction);

        log.info("거래 내역 저장 작업 완료. transactionId: {}", savedTransaction.getId());

        return SaveResponseDto.from(savedTransaction);
    }
}
