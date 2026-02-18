package com.graphflix.ratingservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphflix.ratingservice.exception.EventPublishingException;
import com.graphflix.ratingservice.model.Rating;
import com.graphflix.ratingservice.model.RatingEvent;

@Service
public class RatingEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topic.rating-created:rating-created}")
    private String ratingCreatedTopic;

    @Value("${kafka.topic.rating-updated:rating-updated}")
    private String ratingUpdatedTopic;

    @Value("${kafka.topic.rating-deleted:rating-deleted}")
    private String ratingDeletedTopic;

    public RatingEventProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishRatingCreatedEvent(Rating rating) {
        RatingEvent event = RatingEvent.builder()
                .eventType("RATING_CREATED")
                .ratingId(rating.getId())
                .userId(rating.getUserId())
                .movieId(rating.getMovieId())
                .rating(rating.getRating())
                .comment(rating.getComment())
                .timestamp(rating.getTimestamp())
                .build();

        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(ratingCreatedTopic, event.getMovieId(), json);
        } catch (JsonProcessingException e) {
            throw new EventPublishingException("Failed to publish rating created event", e);
        }
    }

    public void publishRatingUpdatedEvent(Rating rating) {
        RatingEvent event = RatingEvent.builder()
                .eventType("RATING_UPDATED")
                .ratingId(rating.getId())
                .userId(rating.getUserId())
                .movieId(rating.getMovieId())
                .rating(rating.getRating())
                .comment(rating.getComment())
                .timestamp(rating.getTimestamp())
                .build();

        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(ratingUpdatedTopic, event.getMovieId(), json);
        } catch (JsonProcessingException e) {
            throw new EventPublishingException("Failed to publish rating updated event", e);
        }
    }

    public void publishRatingDeletedEvent(Rating rating) {
        RatingEvent event = RatingEvent.builder()
                .eventType("RATING_DELETED")
                .ratingId(rating.getId())
                .userId(rating.getUserId())
                .movieId(rating.getMovieId())
                .rating(rating.getRating())
                .comment(rating.getComment())
                .timestamp(rating.getTimestamp())
                .build();

        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(ratingDeletedTopic, event.getMovieId(), json);
        } catch (JsonProcessingException e) {
            throw new EventPublishingException("Failed to publish rating deleted event", e);
        }
    }
}
