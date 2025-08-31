package com.haydenhurst.bankingapp.auth.exception;

public class JwtGenerationException extends RuntimeException {
    public JwtGenerationException() { super("Failed to generate JWT Token");
    }
}
