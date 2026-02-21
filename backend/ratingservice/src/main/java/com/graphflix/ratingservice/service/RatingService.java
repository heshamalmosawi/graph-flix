package com.graphflix.ratingservice.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.graphflix.ratingservice.dto.AverageRatingDTO;
import com.graphflix.ratingservice.dto.RatingDTO;
import com.graphflix.ratingservice.exception.MovieNotFoundException;
import com.graphflix.ratingservice.exception.RatingNotFoundException;
import com.graphflix.ratingservice.exception.UserNotFoundException;
import com.graphflix.ratingservice.model.Movie;
import com.graphflix.ratingservice.model.Rating;
import com.graphflix.ratingservice.model.User;
import com.graphflix.ratingservice.repository.MovieRepository;
import com.graphflix.ratingservice.repository.RatingRepository;
import com.graphflix.ratingservice.repository.UserRepository;

@Service
public class RatingService {

    private static final Logger log = LoggerFactory.getLogger(RatingService.class);

    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final RatingEventProducer eventProducer;
    private final Neo4jClient neo4jClient;

    public RatingService(RatingRepository ratingRepository, UserRepository userRepository,
            MovieRepository movieRepository, RatingEventProducer eventProducer, Neo4jClient neo4jClient) {
        this.ratingRepository = ratingRepository;
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
        this.eventProducer = eventProducer;
        this.neo4jClient = neo4jClient;
    }

    private void mergeRatedRelationship(String email, String movieTitle, Integer rating, String comment, LocalDateTime timestamp) {
        neo4jClient.query("""
                MATCH (u:User {email: $email})
                MATCH (m:Movie {title: $movieTitle})
                MERGE (u)-[r:RATED]->(m)
                SET r.rating = $rating, r.comment = $comment, r.timestamp = $timestamp
                """)
                .bindAll(Map.of(
                        "email", email,
                        "movieTitle", movieTitle,
                        "rating", rating,
                        "comment", comment != null ? comment : "",
                        "timestamp", timestamp.toString()))
                .run();
        log.info("[RatingService] RATED relationship merged — user: '{}', movie: '{}'", email, movieTitle);
    }

    private void deleteRatedRelationship(String email, String movieTitle) {
        neo4jClient.query("""
                MATCH (u:User {email: $email})-[r:RATED]->(m:Movie {title: $movieTitle})
                DELETE r
                """)
                .bindAll(Map.of("email", email, "movieTitle", movieTitle))
                .run();
        log.info("[RatingService] RATED relationship deleted — user: '{}', movie: '{}'", email, movieTitle);
    }

    @Transactional
    public Rating upsertRating(String email, String movieId, Integer rating, String comment) {
        log.info("[RatingService] upsertRating called — email: '{}', movieId: '{}', rating: {}", email, movieId, rating);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("[RatingService] User not found by email: '{}'", email);
                    return new UserNotFoundException(email);
                });
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> {
                    log.error("[RatingService] Movie not found by id: '{}'", movieId);
                    return new MovieNotFoundException(movieId);
                });

        log.info("[RatingService] User found — id: '{}', name: '{}', email: '{}'", user.getId(), user.getName(), user.getEmail());
        log.info("[RatingService] Movie found — id: '{}', title: '{}'", movie.getId(), movie.getTitle());

        var existing = ratingRepository.findByUserIdAndMovieId(user.getEmail(), movieId);

        if (existing.isPresent()) {
            Rating existingRating = existing.get();
            log.info("[RatingService] Existing rating found — ID: {}, updating", existingRating.getId());
            existingRating.setRating(rating);
            existingRating.setComment(comment);
            existingRating.setTimestamp(LocalDateTime.now());

            Rating updated = ratingRepository.save(existingRating);
            mergeRatedRelationship(user.getEmail(), movie.getTitle(), rating, comment, updated.getTimestamp());
            eventProducer.publishRatingUpdatedEvent(updated);
            log.info("[RatingService] Rating updated successfully — ID: {}", updated.getId());
            return updated;
        }

        LocalDateTime now = LocalDateTime.now();
        Rating newRating = Rating.builder()
                .rating(rating)
                .comment(comment)
                .timestamp(now)
                .userId(user.getEmail())
                .userName(user.getName())
                .movieId(movie.getId())
                .movieTitle(movie.getTitle())
                .build();

        Rating saved = ratingRepository.save(newRating);
        mergeRatedRelationship(user.getEmail(), movie.getTitle(), rating, comment, now);
        eventProducer.publishRatingCreatedEvent(saved);
        log.info("[RatingService] Rating created successfully — ID: {}", saved.getId());
        return saved;
    }

    @Transactional
    public void deleteRating(Long ratingId) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new RatingNotFoundException(ratingId));
        deleteRatedRelationship(rating.getUserId(), rating.getMovieTitle());
        ratingRepository.delete(rating);
        eventProducer.publishRatingDeletedEvent(rating);
    }

    public Page<RatingDTO> getUserRatings(String email, Pageable pageable) {
        Page<Rating> ratings = ratingRepository.findByUserId(email, pageable);
        return ratings.map(this::toDTO);
    }

    public Page<RatingDTO> getMovieRatings(String movieId, Pageable pageable) {
        Page<Rating> ratings = ratingRepository.findByMovieId(movieId, pageable);
        return ratings.map(this::toDTO);
    }

    public RatingDTO getUserRatingForMovie(String email, String movieId) {
        return ratingRepository.findByUserIdAndMovieId(email, movieId)
                .map(this::toDTO)
                .orElse(null);
    }

    public AverageRatingDTO getAverageRating(String movieId) {
        List<Rating> ratings = ratingRepository.findByMovieId(movieId);
        if (ratings.isEmpty()) {
            return AverageRatingDTO.builder()
                    .movieId(movieId)
                    .average(0.0)
                    .count(0L)
                    .build();
        }

        double average = ratings.stream()
                .mapToInt(Rating::getRating)
                .average()
                .orElse(0.0);

        return AverageRatingDTO.builder()
                .movieId(movieId)
                .average(average)
                .count((long) ratings.size())
                .build();
    }

    private RatingDTO toDTO(Rating rating) {
        return RatingDTO.builder()
                .id(rating.getId())
                .rating(rating.getRating())
                .comment(rating.getComment())
                .timestamp(rating.getTimestamp())
                .userId(rating.getUserId())
                .userName(rating.getUserName())
                .movieId(rating.getMovieId())
                .movieTitle(rating.getMovieTitle())
                .build();
    }
}
