package com.graphflix.ratingservice.model;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Node("Rating")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rating {

    @Id
    @GeneratedValue
    private Long id;

    @Property("rating")
    @Min(1)
    @Max(10)
    private Integer rating;

    @Property("comment")
    @Size(max = 500)
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
