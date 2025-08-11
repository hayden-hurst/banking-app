package com.haydenhurst.bankingapp.auth.service;

import com.haydenhurst.bankingapp.auth.dto.LoginRequest;
import com.haydenhurst.bankingapp.auth.dto.SignupRequest;
import com.haydenhurst.bankingapp.auth.exception.InvalidCredentialsException;
import com.haydenhurst.bankingapp.config.JwtConfig;
import com.haydenhurst.bankingapp.user.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class AuthService {
    private final JwtConfig jwtConfig;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthService(UserRepository userRepository,
                       BCryptPasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtConfig jwtConfig) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtConfig = jwtConfig;
    }

    public String registerUser(SignupRequest signupRequest) {
        // first we want to check to make sure the user doesn't already exist with the same email, name, address, phonenumber, etc.
        // then after we verify that this user doesn't already exist we create a new user with the provided info via SignupRequest dto
        // the account will not be verified until they enter their Kyc information which will be used to verify they are who they say they are
        // upon verification they will now be able to own a bank account and send/receive/transfer money.
        return "";
    }

    public String loginUser(LoginRequest loginRequest) {
        try {
            // authenticationManager is the entry point for verifying credentials, it also handles bcrypt comparison automatically
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            List<String> roles = userDetails.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            // create token with Jwts builder
            String token = Jwts.builder()
                    .subject(userDetails.getUsername()) // email
                    .claim("roles", roles)
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + jwtConfig.getJwtExpiration())) // expiration from application.properties
                    .signWith(jwtConfig.getSecretKey()) // secretKey from application.properties
                    .compact();

            return token;
        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException();
        }
    }
}
