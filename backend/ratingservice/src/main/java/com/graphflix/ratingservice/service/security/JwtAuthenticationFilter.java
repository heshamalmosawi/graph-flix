package com.graphflix.ratingservice.service.security;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String method = request.getMethod();
        String uri = request.getRequestURI();
        log.info("[JWT Filter] >>> Incoming request: {} {} ", method, uri);

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("[JWT Filter] No Bearer token found in Authorization header. Header present: {}", authHeader != null);
            filterChain.doFilter(request, response);
            return;
        }
        log.info("[JWT Filter] Bearer token found, proceeding with validation...");

        String token = authHeader.substring(7);

        // Validate token first
        if (!jwtService.validateToken(token)) {
            boolean expired = jwtService.isTokenExpired(token);
            log.error("[JWT Filter] Token validation FAILED. Expired: {}", expired);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            if (expired) {
                response.getWriter().write("Token has expired. Please login again.");
            } else {
                response.getWriter().write("Invalid token");
            }
            return;
        }
        log.info("[JWT Filter] Token signature and expiry validated OK.");

        try {
            // Extract claims
            Claims claims = jwtService.extractAllClaims(token);
            String email = claims.getSubject();
            String id = (String) claims.get("id");

            log.info("[JWT Filter] Token claims — subject(email): '{}', id: '{}', all keys: {}",
                    email, id, claims.keySet());
            log.info("[JWT Filter] Full claims map: {}", claims);

            // Always use email as principal for consistency across all services
            String principal = email;

            if (principal == null) {
                log.error("[JWT Filter] Email (sub claim) is null — cannot authenticate. Skipping auth setup.");
            } else if (SecurityContextHolder.getContext().getAuthentication() != null) {
                log.info("[JWT Filter] SecurityContext already has authentication: {}",
                        SecurityContextHolder.getContext().getAuthentication());
            } else {
                var authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
                var userDetails = new org.springframework.security.core.userdetails.User(principal, "", authorities);
                var auth = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
                log.info("[JWT Filter] Authentication SET in SecurityContext — principal: '{}', authorities: {}",
                        principal, authorities);
            }

        } catch (JwtException e) {
            log.error("[JWT Filter] JwtException while extracting claims: {}", e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            if (e.getMessage().contains("expired")) {
                response.getWriter().write("Token has expired. Please login again.");
            } else {
                response.getWriter().write("Invalid token: " + e.getMessage());
            }
            return;
        }

        log.info("[JWT Filter] <<< Filter complete, passing to next filter. SecurityContext auth: {}",
                SecurityContextHolder.getContext().getAuthentication());
        filterChain.doFilter(request, response);
    }

}
