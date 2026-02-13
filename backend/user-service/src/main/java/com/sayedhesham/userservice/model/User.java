package com.sayedhesham.userservice.model;

import java.time.LocalDateTime;

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
public class User {

    @Id
    private String id;

    @Property("name")
    private String name;

    @Property("username")
    private String username;

    @Property("email")
    private String email;

    @Property("password")
    private String password;

    @Property("role")
    private String role;

    @Property("createdAt")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Property("twoFactorEnabled")
    @Builder.Default
    private Boolean twoFactorEnabled = false;

    // @Relationship(type = "RATED", direction = Relationship.Direction.OUTGOING)
    // @Builder.Default
    // private Set<Rating> ratings = new HashSet<>();
    // @Relationship(type = "IN_WATCHLIST", direction = Relationship.Direction.OUTGOING)
    // @Builder.Default
    // private Set<Movie> watchlist = new HashSet<>();
    // @Relationship(type = "RECOMMENDED", direction = Relationship.Direction.OUTGOING)
    // @Builder.Default
    // private Set<Movie> recommendations = new HashSet<>();
}
