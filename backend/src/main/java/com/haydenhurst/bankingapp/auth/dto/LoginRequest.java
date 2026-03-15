package com.haydenhurst.bankingapp.auth.dto;

public record LoginRequest (
    String email,
    String password
) {}
