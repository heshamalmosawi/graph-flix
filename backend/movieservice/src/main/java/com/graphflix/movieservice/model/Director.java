package com.graphflix.movieservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

import java.time.LocalDate;

@Node
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Director {

    @Id
    private String id;

    @Property("name")
    private String name;

    @Property("birthDate")
    private LocalDate birthDate;
}
