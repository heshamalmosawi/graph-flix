package com.graphflix.movieservice.model;

import java.util.Set;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

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
    @GeneratedValue
    private String id;

    @Property("title")
    private String title;

    @Property("released")
    private Integer released;

    @Property("tagline")
    private String tagline;

    @Relationship(type = "ACTED_IN", direction = Relationship.Direction.INCOMING)
    private Set<Person> actors;

    @Relationship(type = "DIRECTED", direction = Relationship.Direction.INCOMING)
    private Set<Person> directors;
}
