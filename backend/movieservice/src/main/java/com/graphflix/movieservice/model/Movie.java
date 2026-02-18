package com.graphflix.movieservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Node
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

    @Property("description")
    private String description;

    @Property("duration")
    private Integer duration;

    @Property("posterUrl")
    private String posterUrl;

    @Property("imdbRating")
    private Float imdbRating;

    @Relationship(type = "BELONGS_TO_GENRE", direction = Relationship.Direction.OUTGOING)
    @Builder.Default
    private Set<Genre> genres = new HashSet<>();

    @Relationship(type = "ACTED_IN", direction = Relationship.Direction.INCOMING)
    @Builder.Default
    private Set<Actor> actors = new HashSet<>();

    @Relationship(type = "DIRECTED", direction = Relationship.Direction.INCOMING)
    @Builder.Default
    private Set<Director> directors = new HashSet<>();

    @Relationship(type = "RATED", direction = Relationship.Direction.INCOMING)
    @Builder.Default
    private Set<Rating> ratings = new HashSet<>();

    @Relationship(type = "IN_WATCHLIST", direction = Relationship.Direction.INCOMING)
    @Builder.Default
    private Set<User> watchlistUsers = new HashSet<>();

    @Relationship(type = "RECOMMENDED", direction = Relationship.Direction.INCOMING)
    @Builder.Default
    private Set<User> recommendedToUsers = new HashSet<>();

    @Relationship(type = "SIMILAR_TO", direction = Relationship.Direction.UNDIRECTED)
    @Builder.Default
    private Set<Movie> similarMovies = new HashSet<>();
}
