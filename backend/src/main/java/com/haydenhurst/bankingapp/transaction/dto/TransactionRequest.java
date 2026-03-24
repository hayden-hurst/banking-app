package com.haydenhurst.bankingapp.transaction.dto;

import com.haydenhurst.bankingapp.transaction.enums.TransactionRequestType;

import java.math.BigDecimal;

public record TransactionRequest (
        BigDecimal amount,
        TransactionRequestType type,
        String description,
        String destinationAccountNumber
){}
