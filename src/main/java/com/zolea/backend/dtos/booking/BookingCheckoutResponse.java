package com.zolea.backend.dtos.booking;

/**
 * Lo que devuelve crear una reserva: la reserva ya guardada y adónde mandar al cliente a pagar.
 *
 * <p>Existe para que la respuesta esté <b>declarada</b>. Antes se armaba con un
 * {@code Map.of("booking", ..., "paymentUrl", ...)}, así que el JSON salía igual pero la
 * documentación de la API decía apenas «un objeto»: quien la consume no tenía de dónde sacar los
 * nombres de los campos, y un frontend no podía generar sus tipos a partir de ella.
 *
 * @param booking    la reserva creada, en estado PENDING.
 * @param paymentUrl la dirección de MercadoPago donde se paga.
 */
public record BookingCheckoutResponse(
        BookingResponse booking,
        String paymentUrl
) {
}
