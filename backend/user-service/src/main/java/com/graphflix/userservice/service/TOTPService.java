package com.graphflix.userservice.service;

import org.springframework.stereotype.Service;

import com.warrenstrange.googleauth.GoogleAuthenticator;

@Service
public class TOTPService {

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
        System.out.println("[TOTP] Verifying code - Secret: " + secretKey + ", Code: " + code);

        if (secretKey == null || code == null) {
            System.out.println("[TOTP] Secret key or code is null");
            return false;
        }

        try {
            int numericCode = Integer.parseInt(code);
            boolean result = gAuth.authorize(secretKey, numericCode);
            System.out.println("[TOTP] Verification result: " + result);
            return result;
        } catch (NumberFormatException e) {
            System.out.println("[TOTP] Number format exception: " + e.getMessage());
            return false;
        }
    }
}
