package com.haydenhurst.bankingapp.controller;

import com.haydenhurst.bankingapp.dto.LoginRequest;
import com.haydenhurst.bankingapp.dto.SignupRequest;
import com.haydenhurst.bankingapp.model.User;
import com.haydenhurst.bankingapp.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;

// acts as a bridge between the client and the rest of the application
// middleware
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody SignupRequest signupRequest) {
        try{
            String response = userService.registerUser(signupRequest);
            return ResponseEntity.ok("User registered successfully.");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(401).body(ex.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody LoginRequest loginRequest) {
        try {
            String response = userService.loginUser(loginRequest);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(401).body(ex.getMessage()); // 401 Unauthorized
        }
    }
}
