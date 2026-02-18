package com.graphflix.ratingservice.repository;

import java.util.Optional;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import com.graphflix.ratingservice.model.Movie;

@Repository
public interface MovieRepository extends Neo4jRepository<Movie, String> {

    Optional<Movie> findById(String id);
}
