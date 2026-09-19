package com.zolea.backend;

import com.zolea.backend.dtos.booking.BookingRequest;
import com.zolea.backend.dtos.booking.BookingResponse;
import com.zolea.backend.models.Booking;
import com.zolea.backend.models.Client;
import com.zolea.backend.models.RentalUnit;
import com.zolea.backend.models.Status;
import com.zolea.backend.services.BookingService;
import com.zolea.backend.support.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Reglas del dominio que antes del bloque B no se cumplían.
 *
 * <p>Estuvieron deshabilitadas hasta cerrar el bloque: describían el destino y no el presente.
 * Escribirlas antes sirvió para dos cosas. Primero, dejó el criterio de aceptación fijado por
 * escrito en lugar de decidirlo sobre la marcha. Segundo, evitó el error de escribir pruebas de
 * caracterización sobre el comportamiento roto: una prueba que afirmara {@code totalPrice == 0}
 * para una reserva de un día habría que borrarla en la misma semana.
 */
@DisplayName("Reglas del dominio — precio, estados y efectos")
class DomainRulesTest extends IntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Test
    @DisplayName("una reserva de un solo día cobra ese día")
    void price_singleDay_chargesOneDay() {
        Client client = aClient();
        RentalUnit unit = aUnit(12_000.0);
        LocalDate dia = today().plusDays(10);

        BookingResponse response = bookingService.createBooking(new BookingRequest(
                dia, dia, client.getIdClient(), unit.getIdRentalUnit(),
                List.of(), null, null));

        // Antes daba 0.0, y MercadoPago rechaza una preferencia de monto cero.
        // isEqualByComparingTo y no isEqualTo: en BigDecimal, 12000 y 12000.00 son el mismo
        // importe pero no son equals(), porque equals() también compara la escala.
        assertThat(response.totalPrice()).isEqualByComparingTo("12000.00");
    }

    @Test
    @DisplayName("el precio cuenta los dos extremos del rango")
    void price_inclusiveRange_countsBothEnds() {
        Client client = aClient();
        RentalUnit unit = aUnit(10_000.0);
        LocalDate desde = today().plusDays(10);

        BookingResponse response = bookingService.createBooking(new BookingRequest(
                desde, desde.plusDays(2), client.getIdClient(), unit.getIdRentalUnit(),
                List.of(), null, null));

        // Del día 10 al 12 son tres días de uso, y son los tres que la consulta de
        // disponibilidad bloquea. Antes se cobraban dos: el precio y la disponibilidad usaban
        // convenciones distintas.
        assertThat(response.totalPrice()).isEqualByComparingTo("30000.00");
    }

    @Test
    @DisplayName("no se puede confirmar una reserva ya cancelada")
    void confirmPayment_onACanceledBooking_rejected() {
        Client client = aClient();
        RentalUnit unit = aUnit(10_000.0);
        Booking cancelada = aBooking(client, unit, today().plusDays(10), today().plusDays(13),
                Status.CANCELED, now().minusDays(2));

        // Hoy la revive: alcanza con volver a abrir la URL de retorno de MercadoPago.
        assertThatThrownBy(() -> bookingService.confirmBookingPayment(cancelada.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(bookingRepository.findById(cancelada.getId()).orElseThrow().getStatus())
                .isEqualTo(Status.CANCELED);
    }

    @Test
    @DisplayName("confirmar dos veces el mismo pago no duplica huéspedes")
    void confirmPayment_twice_doesNotDuplicateGuests() {
        Client client = aClient();
        RentalUnit unit = aUnit(10_000.0);
        BookingResponse creada = bookingService.createBooking(new BookingRequest(
                today().plusDays(10), today().plusDays(13),
                client.getIdClient(), unit.getIdRentalUnit(),
                List.of(), null, null));

        bookingService.confirmBookingPayment(creada.id());
        bookingService.confirmBookingPayment(creada.id());

        assertThat(bookingService.getBookingById(creada.id()).guests()).hasSize(1);
    }

    @Test
    @DisplayName("consultar una reserva no escribe en la base")
    void readBooking_doesNotWriteToDatabase() {
        Client client = aClient();
        RentalUnit unit = aUnit(10_000.0);
        Booking sinHuespedes = aBooking(client, unit, today().plusDays(10), today().plusDays(13),
                Status.CONFIRMED, now());
        long antes = guestRepository.count();

        bookingService.getBookingById(sinHuespedes.getId());

        // Un GET tiene que ser una lectura. Hoy suma una fila.
        assertThat(guestRepository.count()).isEqualTo(antes);
    }

    @Test
    @DisplayName("una reserva con la fecha de fin anterior al inicio se rechaza")
    void booking_invertedRange_rejected() {
        Client client = aClient();
        RentalUnit unit = aUnit(10_000.0);
        LocalDate desde = today().plusDays(10);

        assertThatThrownBy(() -> bookingService.createBooking(new BookingRequest(
                desde, desde.minusDays(3), client.getIdClient(), unit.getIdRentalUnit(),
                List.of(), null, null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("el QR de una reserva de mostrador lleva el nombre del huésped, no el del mostrador")
    void walkInBooking_qrCarriesTheEnteredName() {
        Client mostrador = aClient(); // el cliente semilla con el que entra la petición
        RentalUnit unit = aUnit(10_000.0);

        BookingResponse response = bookingService.createBooking(new BookingRequest(
                today().plusDays(10), today().plusDays(11),
                mostrador.getIdClient(), unit.getIdRentalUnit(),
                List.of(), "Marta Gómez", "31222333"));

        assertThat(response.guests()).hasSize(1);
        assertThat(response.guests().getFirst().fullName()).isEqualTo("Marta Gómez");
        assertThat(response.guests().getFirst().dni()).isEqualTo("31222333");
    }

    @Test
    @DisplayName("no se puede reservar una unidad bloqueada")
    void booking_blockedUnit_rejected() {
        Client client = aClient();
        RentalUnit unit = aUnit(10_000.0);
        unit.setIsBlocked(true);
        rentalUnitRepository.save(unit);

        assertThatThrownBy(() -> bookingService.createBooking(new BookingRequest(
                today().plusDays(10), today().plusDays(13),
                client.getIdClient(), unit.getIdRentalUnit(),
                List.of(), null, null)))
                .isInstanceOf(RuntimeException.class);
    }
}
