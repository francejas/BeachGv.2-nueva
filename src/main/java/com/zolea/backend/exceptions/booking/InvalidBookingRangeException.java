package com.zolea.backend.exceptions.booking;

/**
 * El rango de fechas de la reserva no tiene sentido.
 *
 * <p>Extiende {@link IllegalArgumentException} a propósito: es literalmente un argumento inválido,
 * y así el dominio se puede probar contra el tipo estándar de Java sin conocer las excepciones de
 * la aplicación. El {@code GlobalExceptionHandler} la traduce a HTTP 400.
 */
public class InvalidBookingRangeException extends IllegalArgumentException {
    public InvalidBookingRangeException(String message) {
        super(message);
    }
}
