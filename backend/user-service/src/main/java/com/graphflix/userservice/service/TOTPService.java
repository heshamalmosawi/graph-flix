package com.graphflix.userservice.service;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import org.springframework.stereotype.Service;

@Service
public class TOTPService {

    private static final String APP_NAME = "GraphFlix";
    private static final int TIME_WINDOW = 1;

    public String generateSecretKey() {
        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        return gAuth.createCredentials().getKey();
    }

    public String generateQRCodeUrl(String email, String secretKey) {
        String issuer = "otpauth://totp/" + APP_NAME;
        return String.format("%s?secret=%s&issuer=%s",
                issuer, secretKey, APP_NAME);
    }

    public boolean verifyCode(String secretKey, String code) {
        if (secretKey == null || code == null) {
            return false;
        }

        try {
            int numericCode = Integer.parseInt(code);
            GoogleAuthenticator gAuth = new GoogleAuthenticator();
            return gAuth.authorize(secretKey, numericCode, TIME_WINDOW);
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
