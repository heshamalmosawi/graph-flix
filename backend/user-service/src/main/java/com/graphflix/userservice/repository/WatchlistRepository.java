package com.graphflix.userservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import com.graphflix.userservice.model.Watchlist;

@Repository
public interface WatchlistRepository extends Neo4jRepository<Watchlist, Long> {

    List<Watchlist> findByUserId(String userId);

    Page<Watchlist> findByUserId(String userId, Pageable pageable);

    Optional<Watchlist> findByUserIdAndMovieId(String userId, String movieId);

    void deleteByUserIdAndMovieId(String userId, String movieId);

    boolean existsByUserIdAndMovieId(String userId, String movieId);

    long countByUserId(String userId);
}
