package com.haydenhurst.bankingapp.auth.service;

import com.haydenhurst.bankingapp.auth.dto.LoginRequest;
import com.haydenhurst.bankingapp.auth.dto.SignupRequest;
import com.haydenhurst.bankingapp.auth.exception.InvalidCredentialsException;
import com.haydenhurst.bankingapp.auth.exception.UserRegistrationException;
import com.haydenhurst.bankingapp.security.JwtTokenProvider;
import com.haydenhurst.bankingapp.user.model.User;
import com.haydenhurst.bankingapp.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public AuthService(UserRepository userRepository,
                       BCryptPasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider, ApplicationContext context) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.context = context;
    }

    private final ApplicationContext context;

    public String registerUser(SignupRequest signupRequest) {
        if (userRepository.existsByEmail(signupRequest.email())) {
            throw new UserRegistrationException("User with this email already exists.");
        }

        if (userRepository.findByPhoneNumber(signupRequest.phoneNumber()).isPresent()) {
            throw new UserRegistrationException("Phone number already in use");
        }

        User user = new User();
        user.setEmail(signupRequest.email());
        user.setHashedPassword(passwordEncoder.encode(signupRequest.password()));
        user.setFullName(signupRequest.fullName());
        user.setPhoneNumber(signupRequest.phoneNumber());
        user.setAddress(signupRequest.address());
        user.setDOB(signupRequest.dob());

        userRepository.save(user);

        return "User registered successfully. Please complete KYC to activate account.";
    }

    public String loginUser(LoginRequest loginRequest) {
        AuthenticationManager authenticationManager = context.getBean(AuthenticationManager.class);

        try {
            Authentication authenticationRequest = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.email(),
                            loginRequest.password()
                    )
            );

            return jwtTokenProvider.generateToken(authenticationRequest);
        } catch (BadCredentialsException ex) {
            throw new InvalidCredentialsException();
        }
    }
}
