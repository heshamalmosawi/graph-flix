package com.graphflix.userservice.model;

import java.time.LocalDateTime;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Node("Watchlist")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Watchlist {

    @Id
    @GeneratedValue
    private Long id;

    @Property("userId")
    private String userId;

    @Property("movieId")
    private String movieId;

    @Property("movieTitle")
    private String movieTitle;

    @Property("addedAt")
    @Builder.Default
    private LocalDateTime addedAt = LocalDateTime.now();
}
