package com.zolea.backend.dtos.booking;

/**
 * Lo que devuelve una reserva de mostrador: ya está confirmada y con sus códigos QR generados.
 *
 * <p>Lleva un mensaje además de la reserva porque el destinatario es el administrador que la acaba
 * de cargar con el cliente enfrente, y lo que necesita saber es si puede entregar los códigos.
 *
 * @param message texto para mostrarle al administrador.
 * @param booking la reserva confirmada, con sus huéspedes y sus códigos.
 */
public record WalkInBookingResponse(
        String message,
        BookingResponse booking
) {
}
