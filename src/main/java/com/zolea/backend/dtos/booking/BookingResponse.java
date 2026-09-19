package com.zolea.backend.dtos.booking;

import com.zolea.backend.dtos.guest.GuestSummaryResponse;
import com.zolea.backend.models.Status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record BookingResponse(
        Long id,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal totalPrice,
        Status status,
        LocalDateTime createdAt,
        Long idClient,
        Long rentalUnitId,
        List<GuestSummaryResponse> guests,
        String walkInName,
        String walkInDni,
        Long resortId,
        String resortName,
        String resortLocation,
        String resortCoverPhotoUrl
) {
}
