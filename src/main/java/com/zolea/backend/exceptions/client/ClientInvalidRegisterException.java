package com.zolea.backend.exceptions.client;

public class ClientInvalidRegisterException extends RuntimeException {
    public ClientInvalidRegisterException(String message) {
        super(message);
    }
}
