package com.haydenhurst.bankingapp.transaction.repository;

import com.haydenhurst.bankingapp.transaction.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findAllByBankAccountId(Long accountId);
    Optional<Transaction> findByIdAndBankAccountId(Long transactionId, Long accountId);
    Page<Transaction> findAllByBankAccountId(Long accountId, Pageable pageable);
}
