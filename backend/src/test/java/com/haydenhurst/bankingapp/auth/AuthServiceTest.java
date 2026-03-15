package com.haydenhurst.bankingapp.auth;

import com.haydenhurst.bankingapp.auth.dto.LoginRequest;
import com.haydenhurst.bankingapp.auth.dto.SignupRequest;
import com.haydenhurst.bankingapp.auth.exception.InvalidCredentialsException;
import com.haydenhurst.bankingapp.auth.exception.UserRegistrationException;
import com.haydenhurst.bankingapp.auth.service.AuthService;
import com.haydenhurst.bankingapp.security.JwtTokenProvider;
import com.haydenhurst.bankingapp.user.model.User;
import com.haydenhurst.bankingapp.user.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private ApplicationContext context;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    // ==================================================
    // Register Tests
    // ==================================================

    @Test
    public void registerUser_success() {
        SignupRequest request = buildSignupRequest();

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.findByPhoneNumber(request.phoneNumber())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.password())).thenReturn("hashed_password");

        String result = authService.registerUser(request);

        assertEquals("User registered successfully. Please complete KYC to activate account.", result);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals(request.email(),       savedUser.getEmail());
        assertEquals("hashed_password",        savedUser.getPassword());
        assertEquals(request.fullName(),    savedUser.getFullName());
        assertEquals(request.phoneNumber(), savedUser.getPhoneNumber());
        assertEquals(request.address(),     savedUser.getAddress());
        assertEquals(request.dob(),         savedUser.getDOB());
    }

    @Test
    void registerUser_throwsWhenEmailAlreadyExists() {
        SignupRequest request = buildSignupRequest();

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        UserRegistrationException ex = assertThrows(
                UserRegistrationException.class,
                () -> authService.registerUser(request)
        );

        assertEquals("User with this email already exists.", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUser_throwsWhenPhoneNumberAlreadyInUse() {
        SignupRequest request = buildSignupRequest();

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.findByPhoneNumber(request.phoneNumber()))
                .thenReturn(Optional.of(new User()));

        UserRegistrationException ex = assertThrows(
                UserRegistrationException.class,
                () -> authService.registerUser(request)
        );

        assertEquals("Phone number already in use", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUser_passwordIsHashed() {
        SignupRequest request = buildSignupRequest();

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.findByPhoneNumber(request.phoneNumber())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.password())).thenReturn("hashed_password");

        authService.registerUser(request);

        verify(passwordEncoder).encode(request.password());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertNotEquals(request.password(), captor.getValue().getPassword());
    }

    // ==================================================
    // Login Tests
    // ==================================================
    @Test
    void loginUser_success() {
        LoginRequest request = buildLoginRequest();
        Authentication authentication = mock(Authentication.class);

        when(context.getBean(AuthenticationManager.class)).thenReturn(authenticationManager);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("jwt_token");

        String result = authService.loginUser(request);

        assertEquals("jwt_token", result);
        verify(jwtTokenProvider).generateToken(authentication);
    }

    @Test
    void loginUser_throwsOnBadCredentials() {
        LoginRequest request = buildLoginRequest();

        when(context.getBean(AuthenticationManager.class)).thenReturn(authenticationManager);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad credentials"));

        assertThrows(InvalidCredentialsException.class, () -> authService.loginUser(request));
    }

    @Test
    void loginUser_passesCorrectCredentialsToAuthManager() {
        LoginRequest request = buildLoginRequest();
        Authentication authentication = mock(Authentication.class);

        when(context.getBean(AuthenticationManager.class)).thenReturn(authenticationManager);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtTokenProvider.generateToken(any())).thenReturn("jwt_token");

        authService.loginUser(request);

        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(captor.capture());

        assertEquals(request.email(),    captor.getValue().getPrincipal());
        assertEquals(request.password(), captor.getValue().getCredentials());
    }

    // ==================================================
    // helper
    // ==================================================
    private SignupRequest buildSignupRequest() {
        return new SignupRequest(
                "test@example.com",
                "plaintext_password",
                "Jane Doe",
                "555-1234",
                "123 Main St",
                LocalDate.of(1990, 1, 1)
        );
    }

    private LoginRequest buildLoginRequest() {
        return new LoginRequest(
            "test@example.com",
            "plaintext_password"
        );
    }
}
