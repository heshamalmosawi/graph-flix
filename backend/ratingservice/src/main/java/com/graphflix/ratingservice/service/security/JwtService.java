package com.graphflix.ratingservice.service.security;

import java.security.Key;
import java.util.Date;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.graphflix.ratingservice.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

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
            var claims = Jwts.parser()
                    .verifyWith((SecretKey) key)
                    .build()
                    .parseSignedClaims(token)
                    .getBody();
            log.debug("[JwtService] Parsed claims: {}", claims);
            return claims;
        } catch (JwtException e) {
            log.error("[JwtService] Failed to parse token: {}", e.getMessage());
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
            boolean expired = isTokenExpired(token);
            log.info("[JwtService] validateToken — expired: {}, valid: {}", expired, !expired);
            return !expired;
        } catch (JwtException e) {
            log.error("[JwtService] validateToken — exception: {}", e.getMessage());
            return false;
        }
    }

    public long getTimeUntilExpiration(String token) throws JwtException {
        Date expirationDate = getExpirationDate(token);
        return expirationDate.getTime() - System.currentTimeMillis();
    }

}
