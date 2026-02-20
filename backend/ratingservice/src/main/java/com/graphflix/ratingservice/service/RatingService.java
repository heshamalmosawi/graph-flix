package com.graphflix.ratingservice.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public RatingService(RatingRepository ratingRepository, UserRepository userRepository,
            MovieRepository movieRepository, RatingEventProducer eventProducer) {
        this.ratingRepository = ratingRepository;
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
        this.eventProducer = eventProducer;
    }

    @Transactional
    public Rating createRating(String userId, String movieId, Integer rating, String comment) {
        log.info("[RatingService] createRating called — userId/email: '{}', movieId: '{}', rating: {}", userId, movieId, rating);

        // userId from JWT is the email address (sub claim), so look up by email
        User user = userRepository.findByEmail(userId)
                .orElseThrow(() -> {
                    log.error("[RatingService] User not found by email: '{}'", userId);
                    return new UserNotFoundException(userId);
                });
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> {
                    log.error("[RatingService] Movie not found by id: '{}'", movieId);
                    return new MovieNotFoundException(movieId);
                });

        log.info("[RatingService] User found — id: '{}', name: '{}', email: '{}'", user.getId(), user.getName(), user.getEmail());
        log.info("[RatingService] Movie found — id: '{}', title: '{}'", movie.getId(), movie.getTitle());

        Rating newRating = Rating.builder()
                .rating(rating)
                .comment(comment)
                .timestamp(LocalDateTime.now())
                .userId(user.getId())
                .userName(user.getName())
                .movieId(movie.getId())
                .movieTitle(movie.getTitle())
                .build();

        Rating saved = ratingRepository.save(newRating);
        eventProducer.publishRatingCreatedEvent(saved);
        log.info("[RatingService] Rating saved successfully — ID: {}", saved.getId());
        return saved;
    }

    @Transactional
    public Rating updateRating(Long ratingId, Integer rating, String comment) {
        Rating existingRating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new RatingNotFoundException(ratingId));

        existingRating.setRating(rating);
        existingRating.setComment(comment);
        existingRating.setTimestamp(LocalDateTime.now());

        Rating updated = ratingRepository.save(existingRating);
        eventProducer.publishRatingUpdatedEvent(updated);
        return updated;
    }

    @Transactional
    public void deleteRating(Long ratingId) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new RatingNotFoundException(ratingId));
        ratingRepository.delete(rating);
        eventProducer.publishRatingDeletedEvent(rating);
    }

    public Page<RatingDTO> getUserRatings(String userId, Pageable pageable) {
        Page<Rating> ratings = ratingRepository.findByUserId(userId, pageable);
        return ratings.map(this::toDTO);
    }

    public Page<RatingDTO> getMovieRatings(String movieId, Pageable pageable) {
        Page<Rating> ratings = ratingRepository.findByMovieId(movieId, pageable);
        return ratings.map(this::toDTO);
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
