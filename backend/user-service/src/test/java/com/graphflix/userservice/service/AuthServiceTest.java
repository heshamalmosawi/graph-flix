package com.graphflix.userservice.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.graphflix.userservice.dto.LoginRequest;
import com.graphflix.userservice.dto.LoginResponse;
import com.graphflix.userservice.dto.RegisterRequest;
import com.graphflix.userservice.model.User;
import com.graphflix.userservice.repository.UserRepository;
import com.graphflix.userservice.service.security.JwtService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
        .id("1")
        .name("John Doe")
        .email("john@example.com")
        .password("encodedPassword")
        .build();
    }

    @Test
    void registerUser_WithValidData_ShouldSaveUser() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Jane Doe");
        request.setEmail("jane@example.com");
        request.setPassword("password12345");

        when(userRepo.findByEmail("jane@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password12345")).thenReturn("encodedPassword");
        when(userRepo.save(any(User.class))).thenReturn(testUser);

        assertDoesNotThrow(() -> authService.registerUser(request));
        verify(userRepo).save(any(User.class));
    }

    @Test
    void registerUser_WithExistingEmail_ShouldThrowException() {
        RegisterRequest request = new RegisterRequest();
        request.setName("John Doe");
        request.setEmail("john@example.com");
        request.setPassword("password12345");

        when(userRepo.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.registerUser(request));
        assertNotNull(exception.getMessage());
    }

    @Test
    void registerUser_WithInvalidEmail_ShouldThrowException() {
        RegisterRequest request = new RegisterRequest();
        request.setName("John Doe");
        request.setEmail("invalid-email");
        request.setPassword("password12345");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.registerUser(request));
        assertNotNull(exception.getMessage());
    }

    @Test
    void registerUser_WithShortPassword_ShouldThrowException() {
        RegisterRequest request = new RegisterRequest();
        request.setName("John Doe");
        request.setEmail("john@example.com");
        request.setPassword("pass");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.registerUser(request));
        assertNotNull(exception.getMessage());
    }

    @Test
    void registerUser_WithoutLetter_ShouldThrowException() {
        RegisterRequest request = new RegisterRequest();
        request.setName("John Doe");
        request.setEmail("john@example.com");
        request.setPassword("12345678");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.registerUser(request));
        assertNotNull(exception.getMessage());
    }

    @Test
    void registerUser_WithoutDigit_ShouldThrowException() {
        RegisterRequest request = new RegisterRequest();
        request.setName("John Doe");
        request.setEmail("john@example.com");
        request.setPassword("password");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.registerUser(request));
        assertNotNull(exception.getMessage());
    }

    @Test
    void loginUser_WithValidCredentials_ShouldReturnLoginResponse() {
        LoginRequest request = new LoginRequest();
        request.setEmail("john@example.com");
        request.setPassword("password12345");

        when(userRepo.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password12345", "encodedPassword")).thenReturn(true);
        when(jwtService.generateToken(any(User.class))).thenReturn("mock-jwt-token");

        LoginResponse result = authService.loginUser(request);

        assertNotNull(result);
        assertNotNull(result.getToken());
        assertTrue(result.getExpiresAt() > System.currentTimeMillis());
    }

    @Test
    void loginUser_WithInvalidEmail_ShouldThrowException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("nonexistent@example.com");
        request.setPassword("password12345");

        when(userRepo.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.loginUser(request));
        assertNotNull(exception.getMessage());
    }

    @Test
    void loginUser_WithInvalidPassword_ShouldThrowException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("john@example.com");
        request.setPassword("wrongpassword");

        when(userRepo.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.loginUser(request));
        assertNotNull(exception.getMessage());
    }
}