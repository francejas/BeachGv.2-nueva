/**
 * Excepciones del dominio y el manejador que las traduce a códigos HTTP.
 *
 * <p>Cada situación tiene su tipo: {@code BookingNotFoundException} sale 404,
 * {@code InvalidBookingRangeException} sale 400, {@code PaymentGatewayException} sale 502. Antes
 * eran doce {@code RuntimeException} genéricas que salían todas como 500, de modo que «no existe» y
 * «se rompió algo» eran indistinguibles para quien consume la API.
 *
 * <p>{@link com.zolea.backend.exceptions.GlobalExceptionHandler} registra el fallo <b>con la
 * excepción adjunta</b> y devuelve un mensaje que no filtra detalle interno. Las dos cosas importan:
 * sin el registro no hay dónde mirar cuando algo se rompe en vivo.
 */
package com.zolea.backend.exceptions;
