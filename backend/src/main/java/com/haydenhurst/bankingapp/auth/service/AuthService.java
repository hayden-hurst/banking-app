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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;
import java.util.List;

@Service
public class AuthService implements UserDetailsService {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public AuthService(UserRepository userRepository,
                       BCryptPasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Autowired
    private ApplicationContext context;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public String registerUser(SignupRequest signupRequest) {
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new UserRegistrationException("User with this email already exists.");
        }

        if (userRepository.findByPhoneNumber(signupRequest.getPhoneNumber()).isPresent()) {
            throw new UserRegistrationException("Phone number already in use");
        }

        User user = new User();
        user.setEmail(signupRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setFullName(signupRequest.getFullName());
        user.setPhoneNumber(signupRequest.getPhoneNumber());
        user.setAddress(signupRequest.getAddress());
        user.setDOB(signupRequest.getDOB());

        userRepository.save(user);

        return "User registered successfully. Please complete KYC to activate account.";
    }

    public String loginUser(LoginRequest loginRequest) {
        AuthenticationManager authenticationManager = context.getBean(AuthenticationManager.class);

        try {
            Authentication authenticationRequest = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            return jwtTokenProvider.generateToken(authenticationRequest);
        } catch (BadCredentialsException ex) {
            throw new InvalidCredentialsException();
        }
    }
}
