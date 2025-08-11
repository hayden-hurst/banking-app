package com.haydenhurst.bankingapp.transaction.repository;

import com.haydenhurst.bankingapp.transaction.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

}
