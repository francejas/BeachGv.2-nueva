package com.zolea.backend;

import com.zolea.backend.models.Booking;
import com.zolea.backend.models.Client;
import com.zolea.backend.models.RentalUnit;
import com.zolea.backend.models.Resort;
import com.zolea.backend.models.Status;
import com.zolea.backend.security.JwtUtil;
import com.zolea.backend.security.UserPrincipal;
import com.zolea.backend.services.BookingService;
import com.zolea.backend.support.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Las escrituras dejan el cambio guardado, no solo lo muestran en la respuesta.
 *
 * <p>Es la red de seguridad de la segunda parte del bloque C. Al declarar las transacciones, los
 * servicios pasan a {@code @Transactional(readOnly = true)} a nivel de clase y cada método que
 * escribe tiene que marcarse como escritura. <b>Olvidarse de uno no rompe ninguna prueba de las
 * que miran la respuesta:</b> en modo lectura Hibernate no vuelca los cambios, pero la entidad en
 * memoria sí quedó modificada, así que el JSON devuelve el valor nuevo y la base conserva el viejo.
 * El error aparecería recién al recargar la página.
 *
 * <p>Por eso cada prueba de acá vuelve a leer de la base después de la petición, en lugar de
 * confiar en el cuerpo de la respuesta.
 */
@AutoConfigureMockMvc
@DisplayName("Escrituras HTTP — el cambio queda guardado")
class HttpWriteTest extends IntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private BookingService bookingService;

    private String tokenFor(Client client) {
        return jwtUtil.generateToken(UserPrincipal.de(client));
    }

    @Test
    @DisplayName("cambiar el precio de una unidad lo guarda")
    void changePrice_isPersisted() throws Exception {
        Resort resort = aResort();
        RentalUnit unidad = aUnitIn(resort, 5_000.0);
        Client admin = anAdminOf(resort);
        syncWithDatabase();

        mockMvc.perform(patch("/api/rental-units/" + unidad.getIdRentalUnit() + "/price")
                        .param("newPrice", "7250.50")
                        .header("Authorization", "Bearer " + tokenFor(admin)))
                .andExpect(status().isOk());

        syncWithDatabase();
        assertThat(rentalUnitRepository.findById(unidad.getIdRentalUnit()).orElseThrow()
                .getDailyPrice()).isEqualByComparingTo("7250.50");
    }

    @Test
    @DisplayName("bloquear una unidad lo guarda")
    void blockUnit_isPersisted() throws Exception {
        Resort resort = aResort();
        RentalUnit unidad = aUnitIn(resort, 5_000.0);
        Client admin = anAdminOf(resort);
        syncWithDatabase();

        mockMvc.perform(patch("/api/rental-units/" + unidad.getIdRentalUnit() + "/block")
                        .param("isBlocked", "true")
                        .header("Authorization", "Bearer " + tokenFor(admin)))
                .andExpect(status().isOk());

        syncWithDatabase();
        assertThat(rentalUnitRepository.findById(unidad.getIdRentalUnit()).orElseThrow()
                .getIsBlocked()).isTrue();
    }

    @Test
    @DisplayName("registrar un cliente lo guarda")
    void registerClient_isPersisted() throws Exception {
        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Nueva","lastName":"Cuenta",
                                 "email":"nueva.cuenta@test.zolea","password":"clave-larga-de-prueba",
                                 "phone":"1155667788","dni":"38111222"}
                                """))
                .andExpect(status().isCreated());

        syncWithDatabase();
        assertThat(clientRepository.findByEmail("nueva.cuenta@test.zolea")).isPresent();
    }

    @Test
    @DisplayName("actualizar el perfil propio lo guarda")
    void updateOwnProfile_isPersisted() throws Exception {
        Client client = aClient();
        syncWithDatabase();

        mockMvc.perform(put("/api/clients/" + client.getIdClient())
                        .header("Authorization", "Bearer " + tokenFor(client))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"NombreNuevo","lastName":"ApellidoNuevo",
                                 "email":"%s","password":null,"phone":"%s","dni":"%s"}
                                """.formatted(client.getEmail(), client.getPhone(), client.getDni())))
                .andExpect(status().isOk());

        syncWithDatabase();
        assertThat(clientRepository.findById(client.getIdClient()).orElseThrow().getFirstName())
                .isEqualTo("NombreNuevo");
    }

    @Test
    @DisplayName("cancelar una reserva propia lo guarda")
    void cancelOwnBooking_isPersisted() throws Exception {
        Client client = aClient();
        Booking booking = aBooking(client, aUnit(5_000.0),
                today().plusDays(5), today().plusDays(7), Status.CONFIRMED, now());
        syncWithDatabase();

        mockMvc.perform(patch("/api/bookings/" + booking.getId() + "/cancel")
                        .header("Authorization", "Bearer " + tokenFor(client)))
                .andExpect(status().isOk());

        syncWithDatabase();
        assertThat(bookingRepository.findById(booking.getId()).orElseThrow().getStatus())
                .isEqualTo(Status.CANCELED);
    }

    @Test
    @DisplayName("crear una unidad en el balneario propio la guarda")
    void createOwnUnit_isPersisted() throws Exception {
        Resort resort = aResort();
        Client admin = anAdminOf(resort);
        syncWithDatabase();

        mockMvc.perform(post("/api/rental-units")
                        .header("Authorization", "Bearer " + tokenFor(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type":"TENT","identifier":"NUEVA-1","dailyPrice":9000.00,
                                 "isBlocked":false,"resortId":%d}
                                """.formatted(resort.getIdResort())))
                .andExpect(status().isCreated());

        syncWithDatabase();
        assertThat(rentalUnitRepository.findByResort_IdResort(resort.getIdResort()))
                .extracting(RentalUnit::getIdentifier)
                .contains("NUEVA-1");
    }

    @Test
    @DisplayName("confirmar el pago de una reserva lo guarda, con su huésped")
    void confirmPayment_isPersisted() throws Exception {
        Client client = aClient();
        Booking booking = aBooking(client, aUnit(new BigDecimal("5000.00")),
                today().plusDays(5), today().plusDays(7), Status.PENDING, now());
        syncWithDatabase();

        // El callback de MercadoPago es el disparador real; acá se invoca el mismo caso de uso.
        bookingService.confirmBookingPayment(booking.getId());

        syncWithDatabase();
        Booking guardada = bookingRepository.findById(booking.getId()).orElseThrow();
        assertThat(guardada.getStatus()).isEqualTo(Status.CONFIRMED);
        assertThat(guestRepository.count()).isEqualTo(1);
    }
}
