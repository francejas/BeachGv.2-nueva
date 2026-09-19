package com.zolea.backend;

import com.zolea.backend.dtos.booking.BookingRequest;
import com.zolea.backend.dtos.booking.BookingResponse;
import com.zolea.backend.models.Client;
import com.zolea.backend.models.Resort;
import com.zolea.backend.models.RentalUnit;
import com.zolea.backend.security.JwtUtil;
import com.zolea.backend.security.UserPrincipal;
import com.zolea.backend.services.BookingService;
import com.zolea.backend.support.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * La plata no pierde centavos.
 *
 * <p>El precio se guardaba en un {@code double}, que no puede representar exactamente la mayoría de
 * los importes con centavos. Con una unidad a $8.500,10 y una reserva de tres días, el total que
 * salía por la API era <b>25500.300000000003</b> en lugar de 25500.30, y ese número era el que se
 * le mandaba a MercadoPago —además con {@code new BigDecimal(double)}, el constructor que arrastra
 * el error binario completo en lugar de redondearlo—.
 *
 * <p>Las dos pruebas miran el <b>JSON</b> y no el tipo Java, a propósito: lo que importa es el
 * contrato que ve quien consume la API, y así la prueba no hay que reescribirla cuando el tipo
 * interno cambia.
 *
 * <p>No se veía en la demostración porque todos los precios del seed son enteros.
 */
@AutoConfigureMockMvc
@DisplayName("Dinero — importes exactos")
class MoneyTest extends IntegrationTest {

    @Autowired private BookingService bookingService;
    @Autowired private MockMvc mockMvc;
    @Autowired private JwtUtil jwtUtil;

    private String tokenFor(Client client) {
        return jwtUtil.generateToken(UserPrincipal.de(client));
    }

    @Test
    @DisplayName("una reserva de tres días a $8.500,10 cobra 25.500,30 exactos")
    void priceWithCents_doesNotCarryDoubleError() throws Exception {
        Client client = aClient();
        RentalUnit unit = aUnit(new BigDecimal("8500.10"));

        BookingResponse creada = bookingService.createBooking(new BookingRequest(
                today().plusDays(10), today().plusDays(12), // tres días, extremos incluidos
                client.getIdClient(), unit.getIdRentalUnit(),
                List.of(), null, null));

        mockMvc.perform(get("/api/bookings/" + creada.id())
                        .header("Authorization", "Bearer " + tokenFor(client)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPrice").value(25500.30));
    }

    /**
     * Fija la forma en que el precio sale serializado.
     *
     * <p>A diferencia de la anterior, esta prueba ya pasaba antes de migrar el tipo: está para que
     * la migración no cambie en silencio lo que ve el frontend, que lee el precio como número.
     */
    @Test
    @DisplayName("el precio diario sale como número, con sus centavos")
    void dailyPrice_serializesAsNumber() throws Exception {
        Resort mio = aResort();
        Client admin = anAdminOf(mio);
        aUnitIn(mio, new BigDecimal("8500.10"));

        mockMvc.perform(get("/api/rental-units")
                        .header("Authorization", "Bearer " + tokenFor(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].dailyPrice").value(8500.10));
    }
}
