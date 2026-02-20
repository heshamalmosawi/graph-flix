package com.graphflix.userservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.graphflix.userservice.dto.PagedWatchlistResponse;
import com.graphflix.userservice.dto.WatchlistDTO;
import com.graphflix.userservice.model.User;
import com.graphflix.userservice.model.Watchlist;
import com.graphflix.userservice.repository.UserRepository;
import com.graphflix.userservice.repository.WatchlistRepository;

@Service
public class WatchlistService {

    private static final Logger log = LoggerFactory.getLogger(WatchlistService.class);

    private final WatchlistRepository watchlistRepository;
    private final UserRepository userRepository;
    private final WatchlistEventProducer eventProducer;

    public WatchlistService(WatchlistRepository watchlistRepository, UserRepository userRepository,
            WatchlistEventProducer eventProducer) {
        this.watchlistRepository = watchlistRepository;
        this.userRepository = userRepository;
        this.eventProducer = eventProducer;
    }

    @Transactional
    public WatchlistDTO addToWatchlist(String email, String movieId, String movieTitle) {
        log.info("[WatchlistService] addToWatchlist called — email: '{}', movieId: '{}', movieTitle: '{}'",
                email, movieId, movieTitle);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("[WatchlistService] User not found by email: '{}'", email);
                    return new RuntimeException("User not found with email: " + email);
                });

        log.info("[WatchlistService] User found — id: '{}', name: '{}', email: '{}'",
                user.getId(), user.getName(), user.getEmail());

        var existing = watchlistRepository.findByUserIdAndMovieId(user.getEmail(), movieId);

        if (existing.isPresent()) {
            log.info("[WatchlistService] Movie already in watchlist — returning existing entry");
            return toDTO(existing.get());
        }

        Watchlist watchlist = Watchlist.builder()
                .userId(user.getEmail())
                .movieId(movieId)
                .movieTitle(movieTitle)
                .build();

        Watchlist saved = watchlistRepository.save(watchlist);
        eventProducer.publishWatchlistAddedEvent(saved);
        log.info("[WatchlistService] Movie added to watchlist successfully — ID: {}", saved.getId());
        return toDTO(saved);
    }

    @Transactional
    public void removeFromWatchlist(String email, String movieId) {
        log.info("[WatchlistService] removeFromWatchlist called — email: '{}', movieId: '{}'", email, movieId);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        Watchlist watchlist = watchlistRepository.findByUserIdAndMovieId(user.getEmail(), movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found in watchlist"));

        eventProducer.publishWatchlistRemovedEvent(watchlist);
        watchlistRepository.deleteByUserIdAndMovieId(user.getEmail(), movieId);
        log.info("[WatchlistService] Movie removed from watchlist successfully");
    }

    public PagedWatchlistResponse getUserWatchlist(String email, Pageable pageable) {
        log.info("[WatchlistService] getUserWatchlist called — email: '{}', page: {}, size: {}",
                email, pageable.getPageNumber(), pageable.getPageSize());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        Page<Watchlist> watchlistPage = watchlistRepository.findByUserId(user.getEmail(), pageable);

        PagedWatchlistResponse response = PagedWatchlistResponse.builder()
                .content(watchlistPage.getContent().stream().map(this::toDTO).toList())
                .currentPage(watchlistPage.getNumber())
                .totalPages(watchlistPage.getTotalPages())
                .totalElements(watchlistPage.getTotalElements())
                .pageSize(watchlistPage.getSize())
                .first(watchlistPage.isFirst())
                .last(watchlistPage.isLast())
                .build();

        log.info("[WatchlistService] Returning {} watchlist items for user '{}'",
                response.getTotalElements(), email);
        return response;
    }

    public boolean isMovieInWatchlist(String email, String movieId) {
        log.info("[WatchlistService] isMovieInWatchlist called — email: '{}', movieId: '{}'", email, movieId);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        boolean exists = watchlistRepository.existsByUserIdAndMovieId(user.getEmail(), movieId);
        log.info("[WatchlistService] Movie in watchlist: {}", exists);
        return exists;
    }

    public long getWatchlistCount(String email) {
        log.info("[WatchlistService] getWatchlistCount called — email: '{}'", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        long count = watchlistRepository.countByUserId(user.getEmail());
        log.info("[WatchlistService] Watchlist count for user '{}': {}", email, count);
        return count;
    }

    private WatchlistDTO toDTO(Watchlist watchlist) {
        return WatchlistDTO.builder()
                .id(watchlist.getId())
                .userId(watchlist.getUserId())
                .movieId(watchlist.getMovieId())
                .movieTitle(watchlist.getMovieTitle())
                .addedAt(watchlist.getAddedAt())
                .build();
    }
}
