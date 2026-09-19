package com.zolea.backend.exceptions.booking;

/**
 * La reserva no está en un estado que permita esa transición.
 *
 * <p>Extiende {@link IllegalStateException} por el mismo motivo que
 * {@link InvalidBookingRangeException} extiende {@code IllegalArgumentException}: describe
 * exactamente lo que pasó y deja el dominio expresable sin depender de tipos propios.
 *
 * <p>Se traduce a HTTP 400, igual que el resto de las violaciones de regla de negocio. Un 409
 * sería más preciso, pero el sistema ya responde 400 a las otras siete reglas del dominio y la
 * coherencia vale más que la precisión acá.
 */
public class BookingStateException extends IllegalStateException {
    public BookingStateException(String message) {
        super(message);
    }
}
