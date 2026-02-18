package com.graphflix.userservice.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.graphflix.userservice.dto.LoginRequest;
import com.graphflix.userservice.dto.LoginResponse;
import com.graphflix.userservice.dto.RegisterRequest;
import com.graphflix.userservice.model.User;
import com.graphflix.userservice.repository.UserRepository;
import com.graphflix.userservice.service.security.JwtService;
import com.graphflix.userservice.util.EmailValidator;

@Service
public class AuthService {

    private static final long TOKEN_EXPIRATION_MS = 1000L * 60 * 60 * 24 * 3; // 3 days in milliseconds
    private static final int MIN_PASSWORD_LENGTH = 6;

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepo, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public void registerUser(RegisterRequest req) {
        validateRegistrationRequest(req);
        validateEmailNotTaken(req.getEmail());

        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .build();

        this.userRepo.save(user);
    }

    private void validateRegistrationRequest(RegisterRequest req) {
        if (req.getName() == null || req.getName().isEmpty()) {
            throw new RuntimeException("Name is required");
        }
        if (req.getEmail() == null || req.getEmail().isEmpty()) {
            throw new RuntimeException("Email is required");
        }
        if (req.getPassword() == null || req.getPassword().isEmpty()) {
            throw new RuntimeException("Password is required");
        }
        EmailValidator.validate(req.getEmail());
        if (req.getPassword().length() < MIN_PASSWORD_LENGTH) {
            throw new RuntimeException("Password must be at least 6 characters long");
        }
    }

    private void validateEmailNotTaken(String email) {
        if (userRepo.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already in use");
        }
    }

    public LoginResponse loginUser(LoginRequest req) {
        User user = userRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }
        return LoginResponse.builder()
                .token(jwtService.generateToken(user))
                .expiresAt(System.currentTimeMillis() + TOKEN_EXPIRATION_MS)
                .name(user.getName())
                .build();
    }
}
