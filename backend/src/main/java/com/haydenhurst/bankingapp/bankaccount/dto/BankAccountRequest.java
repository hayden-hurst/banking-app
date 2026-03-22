package com.haydenhurst.bankingapp.bankaccount.dto;

import com.haydenhurst.bankingapp.bankaccount.enums.BankAccountType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BankAccountRequest(
    @Size(max = 20)
    String accountNickname,
    @NotNull
    BankAccountType bankAccountType
) {}
