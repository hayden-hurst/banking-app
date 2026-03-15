package com.haydenhurst.bankingapp.kyc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

// DTO for KYC verification request
public record KycRequest (
    @NotBlank(message = "SSN is required")
    @Pattern(regexp = "\\d{9}", message = "SSN must be 9 digits")
    String rawSSN,

    @NotBlank(message = "Document type is required")
    String documentType,

    @NotBlank(message = "Document number is required")
    String rawDocumentNumber
) {}
