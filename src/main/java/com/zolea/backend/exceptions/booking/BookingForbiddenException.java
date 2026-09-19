package com.zolea.backend.exceptions.booking;

public class BookingForbiddenException extends RuntimeException {
    public BookingForbiddenException(String message) {
        super(message);
    }
}
