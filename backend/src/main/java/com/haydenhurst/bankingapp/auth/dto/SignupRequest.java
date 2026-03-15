package com.haydenhurst.bankingapp.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record SignupRequest (
    String email,
    String password,
    String fullName,
    String phoneNumber,
    String address,
    @JsonProperty("DOB") LocalDate dob
) {}