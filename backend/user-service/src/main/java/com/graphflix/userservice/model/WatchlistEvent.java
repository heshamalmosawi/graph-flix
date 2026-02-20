package com.graphflix.userservice.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WatchlistEvent {

    private String eventType;
    private Long watchlistId;
    private String userId;
    private String movieId;
    private String movieTitle;
    private LocalDateTime addedAt;
}
