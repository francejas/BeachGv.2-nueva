package com.zolea.backend.payments;

import java.util.Optional;

/**
 * La pasarela de pagos, vista desde la aplicación.
 *
 * <p>Es la primera interfaz de dominio del proyecto: fuera de los repositorios de Spring Data no
 * había ninguna, y por eso «¿cómo probás el flujo de reserva sin pegarle a MercadoPago?» no tenía
 * respuesta. Con el puerto declarado acá, una prueba puede sustituirlo por una implementación de
 * mentira y ejercitar el flujo completo sin red.
 *
 * <p>Los nombres son genéricos —«pasarela», «pago»— y no mencionan MercadoPago a propósito: lo que
 * la aplicación necesita es cobrar, no cobrar <em>con MercadoPago</em>. El proveedor concreto vive
 * en {@link MercadoPagoGateway}.
 */
public interface PaymentGateway {

    /**
     * Prepara un cobro y devuelve la dirección a la que hay que mandar al cliente para pagarlo.
     */
    String createCheckout(CheckoutRequest request);

    /**
     * Le pregunta a la pasarela por un pago concreto.
     *
     * <p>Es lo que permite <b>no</b> creerle al navegador. La dirección a la que la pasarela
     * devuelve al cliente lleva el resultado en la URL, y esa URL la puede escribir cualquiera:
     * la única fuente confiable de si un pago ocurrió es la pasarela misma.
     *
     * @return vacío si la pasarela no conoce ese pago, o si no se la pudo consultar.
     */
    Optional<PaymentStatus> findPayment(String paymentId);
}
