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

@Node
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rating {

    @Id
    @GeneratedValue
    private Long id;

    @Property("rating")
    private Integer rating;

    @Property("comment")
    private String comment;

    @Property("timestamp")
    private LocalDateTime timestamp;

    @Property("userId")
    private String userId;

    @Property("userName")
    private String userName;

    @Property("movieId")
    private String movieId;

    @Property("movieTitle")
    private String movieTitle;
}
