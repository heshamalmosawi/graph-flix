package com.graphflix.recommendationservice.dto;

import com.graphflix.recommendationservice.model.Movie;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieRecommendationDTO {
    
    private String id;
    private String title;
    private Integer released;
    private String tagline;
    private String reason;
    private Double score;
    
    public static MovieRecommendationDTO fromMovie(Movie movie, String reason, Double score) {
        return MovieRecommendationDTO.builder()
            .id(movie.getId())
            .title(movie.getTitle())
            .released(movie.getReleased())
            .tagline(movie.getTagline())
            .reason(reason)
            .score(score)
            .build();
    }
}
