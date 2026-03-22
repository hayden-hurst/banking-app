package com.haydenhurst.bankingapp.bankaccount.dto;

import com.haydenhurst.bankingapp.bankaccount.enums.BankAccountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BankAccountResponse(
    Long id,
    String accountNumber,
    String accountNickname,
    BankAccountType type,
    BigDecimal balance,
    LocalDateTime createdAt
) {}