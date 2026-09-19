package com.zolea.backend.dtos.client;

import com.zolea.backend.dtos.booking.BookingSummaryResponse;
import com.zolea.backend.models.Booking;

import java.util.List;

public record ClientResponse(
        Long idClient,
        String firstName,
        String lastName,
        String email,
        String phone,
        String dni,
        List<BookingSummaryResponse> bookings //lista anidada segura
) {
}