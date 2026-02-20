package com.graphflix.userservice.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WatchlistDTO {

    private Long id;
    private String userId;
    private String movieId;
    private String movieTitle;
    private LocalDateTime addedAt;
}
