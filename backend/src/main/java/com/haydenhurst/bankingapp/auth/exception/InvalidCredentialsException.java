package com.haydenhurst.bankingapp.auth.exception;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Invalid email or password, please try again");
    }
}
