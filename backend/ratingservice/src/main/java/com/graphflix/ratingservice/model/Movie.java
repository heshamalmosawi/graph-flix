package com.graphflix.ratingservice.model;

import java.time.LocalDate;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Node("Movie")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movie {

    @Id
    private String id;

    @Property("title")
    private String title;

    @Property("releaseDate")
    private LocalDate releaseDate;

    @Property("genre")
    private String genre;

    @Property("imdbRating")
    private Float imdbRating;
}
