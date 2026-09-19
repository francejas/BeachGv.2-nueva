package com.zolea.backend.dtos.guest;

public record GuestValidationResponse(
        Long idGuest,
        String fullName,
        String rentalUnitIdentifier,
        String message
) {
}