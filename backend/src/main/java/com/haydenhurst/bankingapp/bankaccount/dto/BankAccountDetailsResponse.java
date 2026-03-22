package com.haydenhurst.bankingapp.bankaccount.dto;

import com.haydenhurst.bankingapp.bankaccount.enums.BankAccountStatus;
import com.haydenhurst.bankingapp.bankaccount.enums.BankAccountType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BankAccountDetailsResponse (
    Long id,
    String accountNumber,
    String accountNickname,
    BankAccountType type,
    BigDecimal balance,
    BankAccountStatus status,
    BigDecimal minimumBalance,
    BigDecimal overdraftLimit,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
){}
