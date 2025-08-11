package com.haydenhurst.bankingapp.bankaccount.repository;

import com.haydenhurst.bankingapp.bankaccount.model.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

}
