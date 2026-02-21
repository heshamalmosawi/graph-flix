package com.graphflix.userservice.config;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

@Configuration
@EnableNeo4jRepositories(basePackages = "com.graphflix.userservice.repository")
public class Neo4jConfig {

    private static final Logger log = LoggerFactory.getLogger(Neo4jConfig.class);

    @Bean
    CommandLineRunner createNeo4jConstraints(Driver driver) {
        return args -> {
            try (Session session = driver.session()) {
                // Deduplicate existing User nodes by email (keep the one with the lowest internal id)
                session.run(
                    "MATCH (u:User) " +
                    "WITH u.email AS email, collect(u) AS nodes " +
                    "WHERE size(nodes) > 1 " +
                    "FOREACH (n IN tail(nodes) | DETACH DELETE n)"
                ).consume();
                log.info("Cleaned up duplicate User nodes (if any)");

                // Create uniqueness constraint so duplicates can never happen again
                session.run(
                    "CREATE CONSTRAINT user_email_unique IF NOT EXISTS " +
                    "FOR (u:User) REQUIRE u.email IS UNIQUE"
                ).consume();
                log.info("Ensured uniqueness constraint on :User(email)");
            } catch (Exception e) {
                log.warn("Could not create Neo4j constraints (may already exist): {}", e.getMessage());
            }
        };
    }
}
