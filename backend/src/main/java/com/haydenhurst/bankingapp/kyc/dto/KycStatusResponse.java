package com.haydenhurst.bankingapp.kyc.dto;

import com.haydenhurst.bankingapp.common.enums.KycStatus;

import java.time.LocalDate;

public record KycStatusResponse(
        KycStatus status,
        LocalDate lastVerifiedOn,
        LocalDate statusChangedOn
) {}