package com.graphflix.ratingservice.controller;

import java.security.Principal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.graphflix.ratingservice.dto.AverageRatingDTO;
import com.graphflix.ratingservice.dto.CreateRatingRequest;
import com.graphflix.ratingservice.dto.RatingDTO;
import com.graphflix.ratingservice.dto.UpdateRatingRequest;
import com.graphflix.ratingservice.model.Rating;
import com.graphflix.ratingservice.service.RatingService;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @PostMapping
    public ResponseEntity<RatingDTO> createRating(
            @RequestBody CreateRatingRequest request,
            @AuthenticationPrincipal Principal principal) {

        String userId = principal.getName();
        Rating rating = ratingService.createRating(
                userId,
                request.getMovieId(),
                request.getRating(),
                request.getComment()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(rating));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RatingDTO> updateRating(
            @PathVariable Long id,
            @RequestBody UpdateRatingRequest request,
            @AuthenticationPrincipal Principal principal) {

        Rating rating = ratingService.updateRating(id, request.getRating(), request.getComment());
        return ResponseEntity.ok(toDTO(rating));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRating(@PathVariable Long id, @AuthenticationPrincipal Principal principal) {
        ratingService.deleteRating(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<RatingDTO>> getUserRatings(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        Page<RatingDTO> ratings = ratingService.getUserRatings(userId, pageable);
        return ResponseEntity.ok(ratings);
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<Page<RatingDTO>> getMovieRatings(
            @PathVariable String movieId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "rating") String sortBy) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        Page<RatingDTO> ratings = ratingService.getMovieRatings(movieId, pageable);
        return ResponseEntity.ok(ratings);
    }

    @GetMapping("/movie/{movieId}/average")
    public ResponseEntity<AverageRatingDTO> getAverageRating(@PathVariable String movieId) {
        AverageRatingDTO averageRating = ratingService.getAverageRating(movieId);
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
