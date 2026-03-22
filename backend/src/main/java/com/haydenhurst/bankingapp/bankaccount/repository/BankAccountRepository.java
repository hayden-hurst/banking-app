package com.haydenhurst.bankingapp.bankaccount.repository;

import com.haydenhurst.bankingapp.bankaccount.model.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    List<BankAccount> findAllByAccountHolderId(Long userId);
    Optional<BankAccount> findByAccountHolderIdAndId(Long holderId, Long accountId);
    boolean existsByAccountNumber(String accountNumber);
}
