package org.example.berichbe.domain.transaction.repository;

import org.example.berichbe.domain.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
