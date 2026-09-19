package com.zolea.backend.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * El registro de un cobro efectivamente realizado.
 *
 * <p>Antes no se guardaba nada del pago: la reserva quedaba confirmada y eso era toda la evidencia.
 * A la pregunta «¿esta reserva se pagó, cuánto y cuándo?» solo se podía responder «está confirmada,
 * así que suponemos que sí».
 *
 * <p>Solo se registran los pagos <b>aprobados</b>. Los rechazados y los pendientes no dejan fila:
 * esta tabla responde «qué se cobró», no «qué se intentó».
 *
 * <p><b>El número de la pasarela es único, y eso es una regla de seguridad, no de prolijidad.</b>
 * Confirmar una reserva ya confirmada no hace nada —eso viene del bloque B— pero eso vale cuando
 * los avisos llegan uno después del otro. Si llegan dos a la vez, los dos leen la reserva como
 * pendiente y los dos siguen: dos huéspedes con dos códigos QR. La restricción de la base es lo que
 * hace que el segundo no entre. Mismo razonamiento que el candado de la doble reserva.
 */
@Table(name = "payment", uniqueConstraints =
        @UniqueConstraint(name = "uk_pago_de_pasarela", columnNames = "gateway_payment_id"))
@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long idPayment;

    /** El identificador del pago en la pasarela. Es lo que impide registrar el mismo dos veces. */
    @Column(name = "gateway_payment_id", nullable = false)
    private String gatewayPaymentId;

    /** El estado tal como lo informó la pasarela, sin traducir. */
    private String status;

    @Column(precision = 12, scale = 2)
    private BigDecimal amount;

    /** Cuándo lo aprobó la pasarela. Puede venir vacío si la pasarela no lo informa. */
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    /** Cuándo nos enteramos nosotros. Sale del reloj inyectado, no del reloj de la máquina. */
    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_booking")
    @ToString.Exclude
    private Booking booking;
}
