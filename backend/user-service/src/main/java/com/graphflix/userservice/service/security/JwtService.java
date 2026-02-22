package com.graphflix.userservice.service.security;

import java.security.Key;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.graphflix.userservice.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final Key key;

    public JwtService(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(User user) {
        return Jwts.builder()
            .setSubject(user.getEmail())
            .claim("name", user.getName())
            .claim("id", user.getId())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 3)) // 3 days
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
    }

    public Claims extractAllClaims(String token) throws JwtException {
        try {
            var x = Jwts.parser()
                    .verifyWith((SecretKey) key)
                    .build()
                    .parseSignedClaims(token)
                    .getBody();
            System.out.println("claims:" + x);
            return x;
        } catch (JwtException e) {
            // catch null, wrong token, expired token
            throw new JwtException(e.getMessage());
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (JwtException e) {
            return true;
        }
    }

    public Date getExpirationDate(String token) throws JwtException {
        return extractAllClaims(token).getExpiration();
    }

    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (JwtException e) {
            return false;
        }
    }

    public long getTimeUntilExpiration(String token) throws JwtException {
        Date expirationDate = getExpirationDate(token);
        return expirationDate.getTime() - System.currentTimeMillis();
    }

    public String generateTemporaryToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("type", "TEMPORARY_2FA")
                .claim("version", user.getTokenVersion())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 5 * 60 * 1000)) // 5 minutes
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTemporaryToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return "TEMPORARY_2FA".equals(claims.get("type"));
        } catch (JwtException e) {
            return false;
        }
    }

    public int extractVersion(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Object version = claims.get("version");
            return version != null ? (int) version : 0;
        } catch (JwtException e) {
            return 0;
        }
    }

    public String extractEmail(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.getSubject();
        } catch (JwtException e) {
            throw new JwtException("Invalid token", e);
        }
    }

}
