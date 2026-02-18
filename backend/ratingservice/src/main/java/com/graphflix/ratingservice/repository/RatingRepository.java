package com.graphflix.ratingservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import com.graphflix.ratingservice.model.Rating;

@Repository
public interface RatingRepository extends Neo4jRepository<Rating, Long> {

    List<Rating> findByUserId(String userId);

    Page<Rating> findByUserId(String userId, Pageable pageable);

    List<Rating> findByMovieId(String movieId);

    Page<Rating> findByMovieId(String movieId, Pageable pageable);

    Optional<Rating> findByUserIdAndMovieId(String userId, String movieId);

    long countByMovieId(String movieId);
}
