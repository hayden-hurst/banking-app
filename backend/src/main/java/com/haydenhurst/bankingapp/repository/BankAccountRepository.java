package com.haydenhurst.bankingapp.repository;

import com.haydenhurst.bankingapp.model.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

}
