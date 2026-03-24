package com.haydenhurst.bankingapp.transaction.dto;

import com.haydenhurst.bankingapp.transaction.enums.TransactionStatus;
import com.haydenhurst.bankingapp.transaction.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse (
        Long id,
        String accountNumber,
        TransactionType type,
        BigDecimal amount,
        String description,
        LocalDateTime timestamp,
        TransactionStatus status
){}
