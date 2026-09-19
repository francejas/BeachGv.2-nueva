package com.zolea.backend.dtos.booking;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BookingSummaryResponse(Long idBooking,
                                     LocalDate startDate,
                                     LocalDate endDate,
                                     BigDecimal totalPrice,
                                     String status,
                                     String rentalUnitIdentifier) {
}
