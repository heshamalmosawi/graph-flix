package com.graphflix.userservice.controller;

import java.security.Principal;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.graphflix.userservice.dto.LoginRequest;
import com.graphflix.userservice.dto.LoginResponse;
import com.graphflix.userservice.dto.RegisterRequest;
import com.graphflix.userservice.dto.TwoFactorRequest;
import com.graphflix.userservice.model.User;
import com.graphflix.userservice.repository.UserRepository;
import com.graphflix.userservice.service.AuthService;
import com.graphflix.userservice.service.TOTPService;
import com.graphflix.userservice.service.security.JwtService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

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

            if (userRepository.isTwoFactorEnabled(user.getEmail())) {
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
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid token type"));
            }

            String email = jwtService.extractEmail(token);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            int tokenVersion = jwtService.extractVersion(token);
            if (user.getTokenVersion() != tokenVersion) {
                return ResponseEntity.badRequest().body(Map.of("error", "Token version mismatch"));
            }

            String secretKey = userRepository.getTotpSecret(email);
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

            return ResponseEntity.badRequest().body(Map.of("error", "Invalid code"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/2fa/setup")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ResponseEntity<Map<String, String>> setup2FA(Principal principal) {
        try {
            String email = principal.getName();
            log.info("[2FA SETUP] Setting up 2FA for user: {}", email);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            log.info("[2FA SETUP] User found: {}", user.getEmail());

            String secretKey = totpService.generateSecretKey();
            log.debug("[2FA SETUP] Generated secret key: {}", secretKey);

            String qrCodeUrl = totpService.generateQRCodeUrl(user.getEmail(), secretKey);
            log.debug("[2FA SETUP] QR Code URL: {}", qrCodeUrl);

            user.setTotpSecret(secretKey);
            User savedUser = userRepository.save(user);
            log.info("[2FA SETUP] User saved: {}", savedUser != null ? "SUCCESS" : "FAILED");
            log.debug("[2FA SETUP] User totpSecret after save: {}", savedUser != null ? savedUser.getTotpSecret() : "NULL");

            return ResponseEntity.ok(Map.of(
                    "qrCode", qrCodeUrl,
                    "secret", secretKey
            ));
        } catch (Exception e) {
            log.error("[2FA SETUP] Error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/2fa/enable")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ResponseEntity<Map<String, String>> enable2FA(
            Principal principal,
            @RequestBody TwoFactorRequest request) {
        try {
            String email = principal.getName();
            log.info("[2FA ENABLE] Enabling 2FA for user: {}", email);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            log.info("[2FA ENABLE] User found: {}", user.getEmail());

            String secretKey = userRepository.getTotpSecret(email);
            log.debug("[2FA ENABLE] Secret key exists: {}", secretKey != null);

            if (secretKey == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "2FA setup required"));
            }

            String code = request.getCode();
            log.debug("[2FA ENABLE] Verification code received");

            boolean isValid = totpService.verifyCode(secretKey, code);
            log.info("[2FA ENABLE] Verification result: {}", isValid);

            if (!isValid) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid verification code"));
            }

            user.setTwoFactorEnabled(true);
            user.setTokenVersion(user.getTokenVersion() + 1);
            User savedUser = userRepository.save(user);
            log.info("[2FA ENABLE] User saved: {}", savedUser != null ? "SUCCESS" : "FAILED");
            return ResponseEntity.ok(Map.of("message", "2FA enabled successfully"));
        } catch (Exception e) {
            log.error("[2FA ENABLE] Error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/2fa/disable")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ResponseEntity<Map<String, String>> disable2FA(
            Principal principal,
            @RequestBody TwoFactorRequest request) {
        try {
            String email = principal.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!userRepository.isTwoFactorEnabled(email)) {
                return ResponseEntity.badRequest().body(Map.of("error", "2FA is not enabled"));
            }

            String secretKey = userRepository.getTotpSecret(email);
            if (!totpService.verifyCode(secretKey, request.getCode())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid code"));
            }

            user.setTotpSecret(null);
            userRepository.setTwoFactorEnabled(email, false);
            userRepository.save(user);
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
