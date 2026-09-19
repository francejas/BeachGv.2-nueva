package com.zolea.backend.exceptions.payment;

/**
 * Falla del proveedor de pagos.
 *
 * <p>Acepta la causa porque es el único punto de integración externa del sistema: sin la excepción
 * original no hay forma de saber si MercadoPago rechazó los datos, se cayó, o el token es inválido.
 */
public class PaymentGatewayException extends RuntimeException {
    public PaymentGatewayException(String message) {
        super(message);
    }

    public PaymentGatewayException(String message, Throwable cause) {
        super(message, cause);
    }
}
