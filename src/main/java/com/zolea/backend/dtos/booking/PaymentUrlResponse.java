package com.zolea.backend.dtos.booking;

/**
 * La dirección de pago de una reserva que ya existe, para cuando el primer intento no se completó.
 *
 * @param paymentUrl la dirección de MercadoPago donde se paga.
 */
public record PaymentUrlResponse(String paymentUrl) {
}
