package com.graphflix.ratingservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.graphflix.ratingservice.dto.AverageRatingDTO;
import com.graphflix.ratingservice.dto.CreateRatingRequest;
import com.graphflix.ratingservice.dto.RatingDTO;
import com.graphflix.ratingservice.model.Rating;
import com.graphflix.ratingservice.service.RatingService;

@RestController
@RequestMapping("/")
public class RatingController {

    private static final Logger log = LoggerFactory.getLogger(RatingController.class);

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @PostMapping
    public ResponseEntity<RatingDTO> upsertRating(
            @RequestBody CreateRatingRequest request) {

        log.info("[RatingController] POST / — upsertRating called with movieId: {}, rating: {}, comment: '{}'",
                request.getMovieId(), request.getRating(), request.getComment());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            log.error("[RatingController] No authenticated user — returning 401");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String userId = auth.getName();
        log.info("[RatingController] Upserting rating for userId: '{}', movieId: '{}', rating: {}",
                userId, request.getMovieId(), request.getRating());

        Rating rating = ratingService.upsertRating(
                userId,
                request.getMovieId(),
                request.getRating(),
                request.getComment()
        );

        log.info("[RatingController] Rating upserted successfully — ID: {}", rating.getId());
        return ResponseEntity.ok(toDTO(rating));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRating(@PathVariable Long id) {
        log.info("[RatingController] DELETE /{} — deleteRating", id);
        ratingService.deleteRating(id);
        log.info("[RatingController] Rating {} deleted successfully", id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<RatingDTO>> getUserRatings(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy) {

        log.info("[RatingController] GET /user/{} — page: {}, size: {}, sortBy: {}", userId, page, size, sortBy);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        Page<RatingDTO> ratings = ratingService.getUserRatings(userId, pageable);
        log.info("[RatingController] Returning {} ratings for user '{}'", ratings.getTotalElements(), userId);
        return ResponseEntity.ok(ratings);
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<Page<RatingDTO>> getMovieRatings(
            @PathVariable String movieId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "rating") String sortBy) {

        log.info("[RatingController] GET /movie/{} — page: {}, size: {}, sortBy: {}", movieId, page, size, sortBy);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        Page<RatingDTO> ratings = ratingService.getMovieRatings(movieId, pageable);
        log.info("[RatingController] Returning {} ratings for movie '{}'", ratings.getTotalElements(), movieId);
        return ResponseEntity.ok(ratings);
    }

    @GetMapping("/movie/{movieId}/average")
    public ResponseEntity<AverageRatingDTO> getAverageRating(@PathVariable String movieId) {
        log.info("[RatingController] GET /movie/{}/average", movieId);
        AverageRatingDTO averageRating = ratingService.getAverageRating(movieId);
        log.info("[RatingController] Average rating for movie '{}': {}", movieId, averageRating);
        return ResponseEntity.ok(averageRating);
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
