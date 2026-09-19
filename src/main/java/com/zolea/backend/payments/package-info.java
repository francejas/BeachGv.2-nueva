/**
 * El puerto de cobro y su adaptador a MercadoPago.
 *
 * <p>{@link com.zolea.backend.payments.PaymentGateway} declara las dos operaciones que el sistema
 * necesita —crear un cobro y consultar un pago—, y
 * {@link com.zolea.backend.payments.MercadoPagoGateway} es <b>el único archivo del proyecto que
 * sabe que la pasarela es MercadoPago</b>. Gracias a eso las pruebas ejercitan el flujo de cobro
 * completo sin red, sustituyendo la implementación por una de mentira.
 *
 * <p>Consultar el pago no es un lujo: es lo que permite no creerle a la URL que abre el navegador.
 * {@code findPayment} devuelve vacío ante cualquier problema —un id con formato raro, un pago
 * inexistente, la pasarela caída— en vez de propagar la excepción, porque quien llama decide con eso
 * si confirmar y la regla es <b>ante la duda no se confirma</b>.
 */
package com.zolea.backend.payments;
