package com.graphflix.ratingservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

@Configuration
@EnableNeo4jRepositories(basePackages = "com.graphflix.ratingservice.repository")
public class Neo4jConfig {
}
