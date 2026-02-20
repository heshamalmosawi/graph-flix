package com.graphflix.userservice.controller;

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

import com.graphflix.userservice.dto.PagedWatchlistResponse;
import com.graphflix.userservice.dto.WatchlistDTO;
import com.graphflix.userservice.service.WatchlistService;

import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/watchlist")
public class WatchlistController {

    private static final Logger log = LoggerFactory.getLogger(WatchlistController.class);

    private final WatchlistService watchlistService;

    public WatchlistController(WatchlistService watchlistService) {
        this.watchlistService = watchlistService;
    }

    public static class AddToWatchlistRequest {
        @NotBlank
        private String movieId;

        @NotBlank
        private String movieTitle;

        public String getMovieId() {
            return movieId;
        }

        public void setMovieId(String movieId) {
            this.movieId = movieId;
        }

        public String getMovieTitle() {
            return movieTitle;
        }

        public void setMovieTitle(String movieTitle) {
            this.movieTitle = movieTitle;
        }
    }

    @PostMapping
    public ResponseEntity<WatchlistDTO> addToWatchlist(@RequestBody AddToWatchlistRequest request) {
        log.info("[WatchlistController] POST / — addToWatchlist called with movieId: {}, movieTitle: '{}'",
                request.getMovieId(), request.getMovieTitle());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            log.error("[WatchlistController] No authenticated user — returning 401");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = auth.getName();
        log.info("[WatchlistController] Adding to watchlist for email: '{}', movieId: '{}'",
                email, request.getMovieId());

        WatchlistDTO watchlistDTO = watchlistService.addToWatchlist(
                email,
                request.getMovieId(),
                request.getMovieTitle()
        );

        log.info("[WatchlistController] Added to watchlist successfully — ID: {}", watchlistDTO.getId());
        return ResponseEntity.ok(watchlistDTO);
    }

    @DeleteMapping("/{movieId}")
    public ResponseEntity<Void> removeFromWatchlist(@PathVariable String movieId) {
        log.info("[WatchlistController] DELETE /{} — removeFromWatchlist", movieId);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = auth.getName();
        log.info("[WatchlistController] Removing from watchlist for email: '{}', movieId: '{}'",
                email, movieId);

        watchlistService.removeFromWatchlist(email, movieId);
        log.info("[WatchlistController] Movie {} removed from watchlist successfully", movieId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<PagedWatchlistResponse> getUserWatchlist(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "addedAt") String sortBy) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = auth.getName();
        log.info("[WatchlistController] GET / — getUserWatchlist for email: '{}', page: {}, size: {}, sortBy: {}",
                email, page, size, sortBy);

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        PagedWatchlistResponse response = watchlistService.getUserWatchlist(email, pageable);
        log.info("[WatchlistController] Returning {} watchlist items for user '{}'",
                response.getTotalElements(), email);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check/{movieId}")
    public ResponseEntity<Boolean> checkMovieInWatchlist(@PathVariable String movieId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = auth.getName();
        log.info("[WatchlistController] GET /check/{} — checkMovieInWatchlist for email: '{}'", movieId, email);

        boolean inWatchlist = watchlistService.isMovieInWatchlist(email, movieId);
        return ResponseEntity.ok(inWatchlist);
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getWatchlistCount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = auth.getName();
        log.info("[WatchlistController] GET /count — getWatchlistCount for email: '{}'", email);

        long count = watchlistService.getWatchlistCount(email);
        return ResponseEntity.ok(count);
    }
}
