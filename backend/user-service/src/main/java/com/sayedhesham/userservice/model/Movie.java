package com.sayedhesham.userservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movie {
    private String id;
    private String title;
    private LocalDate releaseDate;
    private String genre;
    private Float imdbRating;
}
