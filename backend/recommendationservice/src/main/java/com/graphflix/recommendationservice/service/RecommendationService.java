package com.graphflix.recommendationservice.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.graphflix.recommendationservice.dto.MovieRecommendationDTO;
import com.graphflix.recommendationservice.dto.RecommendationResponse;
import com.graphflix.recommendationservice.model.Movie;
import com.graphflix.recommendationservice.repository.RecommendationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecommendationService {
    
    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);
    private static final Integer MIN_RATING = 7;
    
    private final RecommendationRepository recommendationRepository;
    
    public RecommendationResponse getPersonalizedRecommendations(String email, Integer limit) {
        log.info("Getting personalized recommendations for user: {}, limit: {}", email, limit);
        
        Long userRatingCount = recommendationRepository.countUserRatings(email);
        log.info("User {} has {} ratings", email, userRatingCount);
        
        if (userRatingCount == null || userRatingCount < 3) {
            log.info("User {} has insufficient ratings, returning trending movies", email);
            return getTrendingRecommendations(limit);
        }
        
        List<Movie> actorBased = recommendationRepository.findMoviesByLikedActors(email, MIN_RATING, limit);
        log.info("Found {} actor-based recommendations for user {}", actorBased.size(), email);
        
        List<Movie> directorBased = recommendationRepository.findMoviesByLikedDirectors(email, MIN_RATING, limit);
        log.info("Found {} director-based recommendations for user {}", directorBased.size(), email);
        
        Map<String, MovieRecommendationDTO> merged = new LinkedHashMap<>();
        
        actorBased.forEach(movie -> {
            if (!merged.containsKey(movie.getId())) {
                merged.put(movie.getId(), MovieRecommendationDTO.builder()
                    .id(movie.getId())
                    .title(movie.getTitle())
                    .released(movie.getReleased())
                    .tagline(movie.getTagline())
                    .reason("Because you liked movies with these actors")
                    .score(0.8)
                    .build());
            }
        });
        
        directorBased.forEach(movie -> {
            if (merged.containsKey(movie.getId())) {
                MovieRecommendationDTO existing = merged.get(movie.getId());
                existing.setScore(1.0);
                existing.setReason("Because you liked movies with these actors and directors");
            } else {
                merged.put(movie.getId(), MovieRecommendationDTO.builder()
                    .id(movie.getId())
                    .title(movie.getTitle())
                    .released(movie.getReleased())
                    .tagline(movie.getTagline())
                    .reason("Because you liked movies directed by these directors")
                    .score(0.7)
                    .build());
            }
        });
        
        List<MovieRecommendationDTO> recommendations = merged.values().stream()
            .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
            .limit(limit)
            .collect(Collectors.toList());
        
        log.info("Returning {} merged recommendations for user {}", recommendations.size(), email);
        
        return RecommendationResponse.builder()
            .movies(recommendations)
            .build();
    }
    
    public RecommendationResponse getTrendingRecommendations(Integer limit) {
        log.info("Getting trending movies, limit: {}", limit);
        
        List<Movie> trendingMovies = recommendationRepository.findTrendingMovies(limit);
        
        List<MovieRecommendationDTO> recommendations = trendingMovies.stream()
            .map(movie -> MovieRecommendationDTO.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .released(movie.getReleased())
                .tagline(movie.getTagline())
                .reason("Trending now")
                .score(0.5)
                .build())
            .collect(Collectors.toList());
        
        return RecommendationResponse.builder()
            .movies(recommendations)
            .build();
    }
}
