package com.zolea.backend.exceptions.booking;

public class BookingAlreadyCanceledException extends RuntimeException {
    public BookingAlreadyCanceledException(String message) {
        super(message);
    }
}
