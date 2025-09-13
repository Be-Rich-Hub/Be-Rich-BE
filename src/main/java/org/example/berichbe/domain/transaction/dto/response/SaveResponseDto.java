package org.example.berichbe.domain.transaction.dto.response;

import org.example.berichbe.domain.transaction.entity.Transaction;

import java.time.LocalDateTime;

public record SaveResponseDto(
        Long transactionalId,
        LocalDateTime createdAt
) {
  public static SaveResponseDto from(Transaction transaction) {
      return new SaveResponseDto(
              transaction.getId(),
              transaction.getCreatedAt()
      );
  }
}
