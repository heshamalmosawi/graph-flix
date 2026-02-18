package com.graphflix.ratingservice.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingEvent {

    private String eventType;
    private Long ratingId;
    private String userId;
    private String movieId;
    private Integer rating;
    private String comment;
    private LocalDateTime timestamp;
}
