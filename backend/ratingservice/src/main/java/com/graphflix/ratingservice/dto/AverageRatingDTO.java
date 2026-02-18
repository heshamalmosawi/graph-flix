package com.graphflix.ratingservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AverageRatingDTO {

    @JsonProperty("movieId")
    private String movieId;

    @JsonProperty("average")
    private Double average;

    @JsonProperty("count")
    private Long count;
}
