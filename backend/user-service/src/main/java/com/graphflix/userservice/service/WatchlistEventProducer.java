package com.graphflix.userservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphflix.userservice.exception.EventPublishingException;
import com.graphflix.userservice.model.Watchlist;
import com.graphflix.userservice.model.WatchlistEvent;

@Service
public class WatchlistEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topic.watchlist-added:watchlist-added}")
    private String watchlistAddedTopic;

    @Value("${kafka.topic.watchlist-removed:watchlist-removed}")
    private String watchlistRemovedTopic;

    public WatchlistEventProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishWatchlistAddedEvent(Watchlist watchlist) {
        WatchlistEvent event = WatchlistEvent.builder()
                .eventType("WATCHLIST_ADDED")
                .watchlistId(watchlist.getId())
                .userId(watchlist.getUserId())
                .movieId(watchlist.getMovieId())
                .movieTitle(watchlist.getMovieTitle())
                .addedAt(watchlist.getAddedAt())
                .build();

        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(watchlistAddedTopic, event.getUserId(), json);
        } catch (JsonProcessingException e) {
            throw new EventPublishingException("Failed to publish watchlist added event", e);
        }
    }

    public void publishWatchlistRemovedEvent(Watchlist watchlist) {
        WatchlistEvent event = WatchlistEvent.builder()
                .eventType("WATCHLIST_REMOVED")
                .watchlistId(watchlist.getId())
                .userId(watchlist.getUserId())
                .movieId(watchlist.getMovieId())
                .movieTitle(watchlist.getMovieTitle())
                .addedAt(watchlist.getAddedAt())
                .build();

        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(watchlistRemovedTopic, event.getUserId(), json);
        } catch (JsonProcessingException e) {
            throw new EventPublishingException("Failed to publish watchlist removed event", e);
        }
    }
}
