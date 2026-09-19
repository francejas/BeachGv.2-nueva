package com.zolea.backend.payments;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Un pago, tal como lo reporta la pasarela.
 *
 * @param paymentId  el identificador del pago en la pasarela
 * @param status     el estado que informa la pasarela, sin traducir
 * @param amount     lo que se cobró
 * @param bookingId  la reserva a la que corresponde, según la pasarela. Comparar esto con la
 *                   reserva que se quiere confirmar es lo que impide usar el comprobante de un
 *                   pago ajeno para confirmar una reserva propia
 * @param paidAt     cuándo lo aprobó la pasarela. Puede ser nulo: no todas lo informan, y no
 *                   siempre está presente en los pagos que todavía no se aprobaron
 */
public record PaymentStatus(
        String paymentId,
        String status,
        BigDecimal amount,
        Long bookingId,
        LocalDateTime paidAt
) {
    /** El único estado en el que la pasarela da la plata por cobrada. */
    public boolean isApproved() {
        return "approved".equalsIgnoreCase(status);
    }
}
