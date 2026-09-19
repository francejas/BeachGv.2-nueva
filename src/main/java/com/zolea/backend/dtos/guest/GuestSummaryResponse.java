package com.zolea.backend.dtos.guest;

public record GuestSummaryResponse(
        Long idGuest,
        String fullName,
        String dni,
        Boolean isEntryValidated,
        String qrToken
) {
}
