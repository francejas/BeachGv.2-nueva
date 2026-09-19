package com.zolea.backend.exceptions.rentalunit;

/** Ya hay una unidad con ese identificador en ese balneario. */
public class DuplicateIdentifierException extends RuntimeException {
    public DuplicateIdentifierException(String message) {
        super(message);
    }
}
