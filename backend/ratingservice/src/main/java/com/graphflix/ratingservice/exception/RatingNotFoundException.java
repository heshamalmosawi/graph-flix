package com.graphflix.ratingservice.exception;

public class RatingNotFoundException extends RuntimeException {

    public RatingNotFoundException(Long id) {
        super("Rating not found with id: " + id);
    }

    public RatingNotFoundException(String message) {
        super(message);
    }
}
