package com.graphflix.recommendationservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.graphflix.recommendationservice.dto.RecommendationResponse;
import com.graphflix.recommendationservice.service.RecommendationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class RecommendationController {
    
    private final RecommendationService recommendationService;
    
    @GetMapping("/personalized")
    public ResponseEntity<RecommendationResponse> getPersonalizedRecommendations(
        @AuthenticationPrincipal User user,
        @RequestParam(defaultValue = "10") Integer limit
    ) {
        String email = user.getUsername();
        log.info("GET /recommendations/personalized - email: {}, limit: {}", email, limit);
        
        if (limit < 1 || limit > 50) {
            limit = 10;
        }
        
        RecommendationResponse recommendations = recommendationService.getPersonalizedRecommendations(email, limit);
        return ResponseEntity.ok(recommendations);
    }
    
    @GetMapping("/trending")
    public ResponseEntity<RecommendationResponse> getTrendingRecommendations(
        @RequestParam(defaultValue = "10") Integer limit
    ) {
        log.info("GET /recommendations/trending - limit: {}", limit);
        
        if (limit < 1 || limit > 50) {
            limit = 10;
        }
        
        RecommendationResponse recommendations = recommendationService.getTrendingRecommendations(limit);
        return ResponseEntity.ok(recommendations);
    }
}
