package com.graphflix.userservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.graphflix.userservice.dto.LoginRequest;
import com.graphflix.userservice.dto.LoginResponse;
import com.graphflix.userservice.dto.RegisterRequest;
import com.graphflix.userservice.dto.TwoFactorRequest;
import com.graphflix.userservice.dto.TwoFactorSetupResponse;
import com.graphflix.userservice.model.User;
import com.graphflix.userservice.repository.UserRepository;
import com.graphflix.userservice.service.AuthService;
import com.graphflix.userservice.service.TOTPService;
import com.graphflix.userservice.service.security.JwtAuthenticationFilter;
import com.graphflix.userservice.service.security.JwtService;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private TOTPService totpService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        try {
            authService.registerUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody LoginRequest request) {
        try {
            User user = authService.findUserByEmail(request.getEmail());

            if (userRepository.isTwoFactorEnabled(user.getId())) {
                String tempToken = jwtService.generateTemporaryToken(user);
                long expiryTime = System.currentTimeMillis() + 5 * 60 * 1000L;
                return ResponseEntity.ok(LoginResponse.builder()
                        .token(tempToken)
                        .name(user.getName())
                        .expiresAt(expiryTime)
                        .status("2FA_REQUIRED")
                        .message("Please complete two-factor authentication")
                        .build());
            }

            LoginResponse resp = authService.loginUser(request);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/verify-2fa")
    public ResponseEntity<Object> verify2FA(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody TwoFactorRequest request) {
        try {
            String token = extractToken(authHeader);

            if (!jwtService.isTemporaryToken(token)) {
                return ResponseEntity.badRequest().body("Invalid token type");
            }

            String userId = jwtService.extractUserId(token);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            int tokenVersion = jwtService.extractVersion(token);
            if (user.getTokenVersion() != tokenVersion) {
                return ResponseEntity.badRequest().body("Token version mismatch");
            }

            String secretKey = userRepository.getTotpSecret(userId);
            if (totpService.verifyCode(secretKey, request.getCode())) {
                long expiryTime = System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000L;
                return ResponseEntity.ok(LoginResponse.builder()
                        .token(jwtService.generateToken(user))
                        .name(user.getName())
                        .expiresAt(expiryTime)
                        .status("SUCCESS")
                        .message("Two-factor authentication successful")
                        .build());
            }

            return ResponseEntity.badRequest().body("Invalid code");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/2fa/setup")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> setup2FA(Principal principal) {
        try {
            String userId = principal.getName();
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String secretKey = totpService.generateSecretKey();
            String qrCodeUrl = totpService.generateQRCodeUrl(user.getEmail(), secretKey);

            userRepository.setTotpSecret(userId, secretKey);

            return ResponseEntity.ok(Map.of(
                    "qrCode", qrCodeUrl,
                    "secret", secretKey
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/2fa/enable")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> enable2FA(
            Principal principal,
            @RequestBody TwoFactorRequest request) {
        try {
            String userId = principal.getName();
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String secretKey = userRepository.getTotpSecret(userId);
            if (secretKey == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "2FA setup required"));
            }

            if (!totpService.verifyCode(secretKey, request.getCode())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid verification code"));
            }

            userRepository.setTwoFactorEnabled(userId, true);
            return ResponseEntity.ok(Map.of("message", "2FA enabled successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/2fa/disable")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> disable2FA(
            Principal principal,
            @RequestBody TwoFactorRequest request) {
        try {
            String userId = principal.getName();
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!userRepository.isTwoFactorEnabled(userId)) {
                return ResponseEntity.badRequest().body(Map.of("error", "2FA is not enabled"));
            }

            String secretKey = userRepository.getTotpSecret(userId);
            if (!totpService.verifyCode(secretKey, request.getCode())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid code"));
            }

            userRepository.setTwoFactorEnabled(userId, false);
            userRepository.setTotpSecret(userId, null);
            return ResponseEntity.ok(Map.of("message", "2FA disabled successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

}
