package com.zolea.backend.exceptions.guest;

public class BookingNotPaidException extends RuntimeException {
    public BookingNotPaidException(String message) {
        super(message);
    }
}
