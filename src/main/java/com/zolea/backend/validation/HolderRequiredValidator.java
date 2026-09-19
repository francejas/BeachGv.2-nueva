package com.zolea.backend.validation;

import com.zolea.backend.dtos.booking.BookingRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/** Comprueba {@link HolderRequired} sobre una petición de reserva. */
public class HolderRequiredValidator implements ConstraintValidator<HolderRequired, BookingRequest> {

    @Override
    public boolean isValid(BookingRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true; // de un pedido nulo se encarga @NotNull, no esta restricción
        }
        boolean hayCliente = request.clientId() != null && request.clientId() > 0;
        boolean hayMostrador = request.walkInName() != null && !request.walkInName().isBlank();
        return hayCliente || hayMostrador;
    }
}
