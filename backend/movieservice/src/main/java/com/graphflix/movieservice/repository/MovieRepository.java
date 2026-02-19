package com.graphflix.movieservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import com.graphflix.movieservice.model.Movie;

@Repository
public interface MovieRepository extends Neo4jRepository<Movie, String> {

    Optional<Movie> findById(String id);

    Page<Movie> findAll(Pageable pageable);

    List<Movie> findByTitleContainingIgnoreCase(String title);

    List<Movie> findByReleasedBetween(Integer startYear, Integer endYear);

    @Query("MATCH (m:Movie)<-[:ACTED_IN]-(p:Person) WHERE p.name CONTAINS $personName RETURN m")
    List<Movie> findByPersonName(String personName);
}
