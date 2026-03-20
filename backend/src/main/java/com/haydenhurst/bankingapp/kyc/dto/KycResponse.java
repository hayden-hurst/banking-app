package com.haydenhurst.bankingapp.kyc.dto;

import com.haydenhurst.bankingapp.common.enums.KycStatus;

import java.time.LocalDate;

public record KycResponse(
        Long id,
        String documentType,
        KycStatus status,
        LocalDate lastVerifiedOn,
        LocalDate statusChangedOn
) {}
