package com.zolea.backend.exceptions.amenity;

public class AmenityNotFoundException extends RuntimeException {
    public AmenityNotFoundException(String message) {
        super(message);
    }
}