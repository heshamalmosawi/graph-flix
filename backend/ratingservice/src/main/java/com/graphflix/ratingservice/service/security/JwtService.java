package com.graphflix.ratingservice.service.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.graphflix.ratingservice.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final SecretKey key;

    public JwtService(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(User user) {
        Instant now = Instant.now();
        Instant expiration = now.plus(3, ChronoUnit.DAYS);

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("name", user.getName())
                .claim("id", user.getId())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public Claims extractAllClaims(String token) throws JwtException {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            System.out.println("claims:" + claims);
            return claims;
        } catch (JwtException e) {
            throw new JwtException(e.getMessage());
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.getExpiration().before(Date.from(Instant.now()));
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

}
