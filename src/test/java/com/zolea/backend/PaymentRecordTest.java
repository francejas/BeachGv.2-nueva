package com.zolea.backend;

import com.zolea.backend.models.Booking;
import com.zolea.backend.models.Client;
import com.zolea.backend.models.Payment;
import com.zolea.backend.models.Status;
import com.zolea.backend.payments.CheckoutRequest;
import com.zolea.backend.payments.PaymentGateway;
import com.zolea.backend.payments.PaymentStatus;
import com.zolea.backend.repositories.PaymentRepository;
import com.zolea.backend.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * De cada cobro queda registro.
 *
 * <p>Hasta ahora el sistema confirmaba la reserva y no guardaba <b>nada</b> del pago: ni el número
 * que le da MercadoPago, ni el importe que efectivamente se cobró, ni cuándo. Si alguien preguntaba
 * «¿esta reserva se pagó, cuánto y cuándo?», la única respuesta era «está confirmada, así que
 * suponemos que sí».
 *
 * <p>El registro además cierra un agujero que la idempotencia en memoria no cubría. Confirmar una
 * reserva ya confirmada no hace nada —eso viene del bloque B— pero eso vale cuando los avisos llegan
 * <b>uno después del otro</b>. Si llegan dos a la vez, los dos leen la reserva como pendiente y los
 * dos siguen: dos huéspedes, dos códigos QR. Con el número de pago marcado como único en la base,
 * el segundo no entra. Es el mismo razonamiento que el candado de la doble reserva.
 */
@AutoConfigureMockMvc
@Import(PaymentRecordTest.FakePaymentGateway.class)
@DisplayName("Pagos — queda registro de lo cobrado")
class PaymentRecordTest extends IntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private FakePaymentGateway gateway;
    @Autowired private PaymentRepository paymentRepository;

    @BeforeEach
    void clearTheGateway() {
        gateway.forgetEverything();
    }

    private Booking aPendingBooking() {
        return aBooking(aClient(), aUnit(new BigDecimal("10000.00")),
                today().plusDays(10), today().plusDays(12), Status.PENDING, now());
    }

    private void mercadoPagoNotifies(String paymentId) throws Exception {
        mockMvc.perform(post("/api/bookings/notifications")
                .param("type", "payment")
                .param("data.id", paymentId));
    }

    // ---------------------------------------------------------------------

    @Test
    @DisplayName("al confirmar queda guardado el número, el importe y la fecha del pago")
    void confirming_storesTheNumberAmountAndDate() throws Exception {
        Booking booking = aPendingBooking();
        LocalDateTime paidAt = now().minusMinutes(5);
        gateway.registerPayment("MP-1", "approved", booking.getId(),
                new BigDecimal("30000.00"), paidAt);
        syncWithDatabase();

        mercadoPagoNotifies("MP-1");

        syncWithDatabase();
        Payment payment = paymentRepository.findByGatewayPaymentId("MP-1").orElseThrow();
        assertThat(payment.getAmount()).isEqualByComparingTo("30000.00");
        assertThat(payment.getStatus()).isEqualTo("approved");
        // Truncado a milisegundos de los dos lados: MySQL guarda microsegundos y Java tiene
        // nanosegundos, así que lo que vuelve de la base nunca es idéntico bit a bit a lo que se
        // guardó. Lo que la prueba fija es la fecha y la hora del cobro, no la precisión del motor.
        assertThat(payment.getPaidAt().truncatedTo(ChronoUnit.MILLIS))
                .isEqualTo(paidAt.truncatedTo(ChronoUnit.MILLIS));
        assertThat(payment.getBooking().getId()).isEqualTo(booking.getId());
    }

    @Test
    @DisplayName("el mismo pago avisado tres veces deja un solo registro")
    void theSamePaymentNotifiedThreeTimes_leavesOneRecord() throws Exception {
        Booking booking = aPendingBooking();
        gateway.registerPayment("MP-2", "approved", booking.getId(),
                new BigDecimal("10000.00"), now());
        syncWithDatabase();

        mercadoPagoNotifies("MP-2");
        mercadoPagoNotifies("MP-2");
        mercadoPagoNotifies("MP-2");

        syncWithDatabase();
        assertThat(paymentRepository.count())
                .withFailMessage("tres avisos del mismo pago dejaron más de un registro")
                .isEqualTo(1);
    }

    @Test
    @DisplayName("el aviso y el retorno del mismo pago dejan un solo registro")
    void theNotificationAndTheReturnOfTheSamePayment_leaveOneRecord() throws Exception {
        Booking booking = aPendingBooking();
        gateway.registerPayment("MP-4", "approved", booking.getId(),
                new BigDecimal("10000.00"), now());
        syncWithDatabase();

        // Los dos caminos de confirmación, uno detrás del otro, como pasa en la vida real: el aviso
        // llega apenas se acredita el pago y el cliente vuelve del sitio de MercadoPago un segundo
        // después. Los dos hablan del mismo pago.
        mercadoPagoNotifies("MP-4");
        mockMvc.perform(get("/api/bookings/success")
                        .param("collection_id", "MP-4")
                        .param("external_reference", String.valueOf(booking.getId())))
                .andExpect(status().is3xxRedirection());

        syncWithDatabase();
        assertThat(paymentRepository.count())
                .withFailMessage("los dos caminos dejaron dos registros del mismo pago")
                .isEqualTo(1);
    }

    @Test
    @DisplayName("un pago no aprobado no se registra")
    void anUnapprovedPayment_isNotRecorded() throws Exception {
        Booking booking = aPendingBooking();
        gateway.registerPayment("MP-3", "rejected", booking.getId(),
                new BigDecimal("10000.00"), now());
        syncWithDatabase();

        mercadoPagoNotifies("MP-3");

        syncWithDatabase();
        assertThat(paymentRepository.count()).isZero();
    }

    // ---------------------------------------------------------------------

    /** Una pasarela que solo conoce los pagos que la prueba le registra. */
    @TestConfiguration
    public static class FakePaymentGateway implements PaymentGateway {

        private final Map<String, PaymentStatus> payments = new HashMap<>();

        @Bean
        @Primary
        PaymentGateway fakePaymentGateway() {
            return this;
        }

        void registerPayment(String id, String status, Long bookingId,
                             BigDecimal amount, LocalDateTime paidAt) {
            payments.put(id, new PaymentStatus(id, status, amount, bookingId, paidAt));
        }

        void forgetEverything() {
            payments.clear();
        }

        @Override
        public String createCheckout(CheckoutRequest request) {
            return "https://pasarela.de.mentira/checkout/" + request.bookingId();
        }

        @Override
        public Optional<PaymentStatus> findPayment(String paymentId) {
            return Optional.ofNullable(payments.get(paymentId));
        }
    }
}
