package com.graphflix.userservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.warrenstrange.googleauth.GoogleAuthenticator;

@Service
public class TOTPService {

    private static final Logger log = LoggerFactory.getLogger(TOTPService.class);
    private static final String APP_NAME = "GraphFlix";

    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    public String generateSecretKey() {
        return gAuth.createCredentials().getKey();
    }

    public String generateQRCodeUrl(String email, String secretKey) {
        return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s",
                APP_NAME, email, secretKey, APP_NAME);
    }

    public boolean verifyCode(String secretKey, String code) {
        log.debug("[TOTP] Verifying code - Secret: {}, Code: {}", secretKey != null ? "***" : "null", code != null ? "***" : "null");

        if (secretKey == null || code == null) {
            log.warn("[TOTP] Secret key or code is null");
            return false;
        }

        try {
            int numericCode = Integer.parseInt(code);
            boolean result = gAuth.authorize(secretKey, numericCode);
            log.info("[TOTP] Verification result: {}", result);
            return result;
        } catch (NumberFormatException e) {
            log.error("[TOTP] Number format exception: {}", e.getMessage());
            return false;
        }
    }
}
