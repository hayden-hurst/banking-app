package com.haydenhurst.bankingapp.transaction.dto;

import com.haydenhurst.bankingapp.transaction.enums.TransactionType;

import java.math.BigDecimal;

public record TransactionRequest (
        BigDecimal amount,
        TransactionType type,
        String description
){}
