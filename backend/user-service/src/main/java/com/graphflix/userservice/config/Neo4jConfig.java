package com.graphflix.userservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

@Configuration
@EnableNeo4jRepositories(basePackages = "com.graphflix.userservice.repository")
public class Neo4jConfig {
}
