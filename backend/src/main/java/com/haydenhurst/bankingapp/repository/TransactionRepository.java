package com.haydenhurst.bankingapp.repository;

import com.haydenhurst.bankingapp.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

}
