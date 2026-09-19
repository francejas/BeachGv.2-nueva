package com.zolea.backend.exceptions.resort;

public class ResortNotFoundException extends RuntimeException {
    public ResortNotFoundException(String message) {
        super(message);
    }
}
