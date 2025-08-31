package com.haydenhurst.bankingapp.auth.controller;

import com.haydenhurst.bankingapp.auth.dto.LoginRequest;
import com.haydenhurst.bankingapp.auth.dto.SignupRequest;
import com.haydenhurst.bankingapp.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody SignupRequest signupRequest) {
        String response = authService.registerUser(signupRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody LoginRequest loginRequest) {
        String response = authService.loginUser(loginRequest);
        return ResponseEntity.ok(response);
    }
}

