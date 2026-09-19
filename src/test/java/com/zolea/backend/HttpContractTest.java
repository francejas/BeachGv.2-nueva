package com.zolea.backend;

import com.zolea.backend.models.Booking;
import com.zolea.backend.models.Client;
import com.zolea.backend.models.RentalUnit;
import com.zolea.backend.models.Status;
import com.zolea.backend.security.JwtUtil;
import com.zolea.backend.security.UserPrincipal;
import com.zolea.backend.support.IntegrationTest;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * El código de estado HTTP que devuelve la API en los caminos de error.
 *
 * <p>Antes del bloque C casi todo error salía como 500, y eso tenía dos consecuencias. La primera
 * es que el frontend no podía reaccionar: solo sabe qué hacer con un 401 (mandar al login) y con un
 * 404 (mostrar «no existe»). La segunda es que un 403 disfrazado de 500 hacía que el control de
 * permisos <em>pareciera</em> roto, lo que volvía imposible verificar la autorización.
 *
 * <p>Por eso este contrato se cerró ANTES de tocar la autorización, y no después.
 */
@AutoConfigureMockMvc
@DisplayName("Contrato HTTP — el código de error que corresponde")
class HttpContractTest extends IntegrationTest {

    /** Un id que no existe en ninguna tabla: el esquema se recrea en cada corrida. */
    private static final long MISSING_ID = 999_999L;

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtUtil jwtUtil;

    /** El mismo secreto que usa la aplicación, para poder firmar un token vencido a mano. */
    @Value("${JWT_SECRET}")
    private String jwtSecret;

    private String tokenFor(Client client) {
        return jwtUtil.generateToken(UserPrincipal.de(client));
    }

    /**
     * Un token bien firmado pero vencido hace una hora. No se puede producir con
     * {@code JwtUtil.generateToken}, que siempre emite una hora hacia adelante.
     */
    private String expiredTokenFor(Client client) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        long ahora = System.currentTimeMillis();
        return Jwts.builder()
                .subject(client.getEmail())
                .issuedAt(new Date(ahora - 7_200_000L))
                .expiration(new Date(ahora - 3_600_000L))
                .signWith(key)
                .compact();
    }

    // ---------------------------------------------------------------------
    // 404 — lo que no existe
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("pedir una reserva que no existe devuelve 404")
    void unknownBooking_returns404() throws Exception {
        Client usuario = aClient();

        mockMvc.perform(get("/api/bookings/" + MISSING_ID)
                        .header("Authorization", "Bearer " + tokenFor(usuario)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("cambiar el precio de una unidad que no existe devuelve 404")
    void unknownUnit_returns404() throws Exception {
        // Con balneario asignado a propósito: un administrador sin balneario no gestiona ninguna
        // unidad, así que para él la respuesta correcta es 403 y no llegaría nunca al 404.
        Client admin = anAdminOf(aResort());

        mockMvc.perform(patch("/api/rental-units/" + MISSING_ID + "/price")
                        .param("newPrice", "5000")
                        .header("Authorization", "Bearer " + tokenFor(admin)))
                .andExpect(status().isNotFound());
    }

    /**
     * El 403 gana al 404, y es deliberado.
     *
     * <p>Esta prueba esperaba un 404 antes del bloque A.2, cuando cualquier administrador podía
     * crear unidades en cualquier balneario y el único error posible era «ese balneario no existe».
     * Ahora la pertenencia se comprueba primero: un balneario que no existe tampoco es el tuyo, así
     * que la respuesta es 403 y el 404 dejó de ser alcanzable por acá.
     *
     * <p>Es la excepción consciente al criterio general de que 404 significa «no está»: cuando la
     * regla de acceso se puede evaluar <em>sin</em> leer el recurso, se evalúa antes, porque
     * responder 404 obligaría a confirmar primero que el recurso ajeno existe.
     */
    @Test
    @DisplayName("crear una unidad en un balneario que no existe devuelve 403, no 404")
    void unknownResort_returns403() throws Exception {
        Client admin = anAdminOf(aResort());

        mockMvc.perform(post("/api/rental-units")
                        .header("Authorization", "Bearer " + tokenFor(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "UMBRELLA",
                                  "identifier": "Z-1",
                                  "dailyPrice": 5000,
                                  "isBlocked": false,
                                  "resortId": %d
                                }
                                """.formatted(MISSING_ID)))
                .andExpect(status().isForbidden());
    }

    // ---------------------------------------------------------------------
    // 400 — la petición es válida como HTTP pero rompe una regla
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("cancelar una reserva ya cancelada devuelve 400, no 500")
    void cancelAlreadyCanceledBooking_returns400() throws Exception {
        Client usuario = aClient();
        RentalUnit unidad = aUnit(5000.0);
        Booking cancelada = aBooking(usuario, unidad,
                today().plusDays(3), today().plusDays(5), Status.CANCELED, now());

        mockMvc.perform(patch("/api/bookings/" + cancelada.getId() + "/cancel")
                        .header("Authorization", "Bearer " + tokenFor(usuario)))
                .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------------------
    // 401 — el problema es la credencial
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("un token vencido devuelve 401, no 500")
    void expiredToken_returns401() throws Exception {
        Client usuario = aClient();

        mockMvc.perform(get("/api/bookings")
                        .header("Authorization", "Bearer " + expiredTokenFor(usuario)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("un token ilegible devuelve 401, no 500")
    void unreadableToken_returns401() throws Exception {
        mockMvc.perform(get("/api/bookings")
                        .header("Authorization", "Bearer esto-no-es-un-jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("sin token, un endpoint protegido devuelve 401")
    void noToken_returns401() throws Exception {
        mockMvc.perform(get("/api/bookings"))
                .andExpect(status().isUnauthorized());
    }

    // ---------------------------------------------------------------------
    // 403 — la credencial es válida pero el rol no alcanza
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("un usuario común en un endpoint de admin devuelve 403, no 500")
    void regularUserOnAdminEndpoint_returns403() throws Exception {
        Client usuario = aClient();

        mockMvc.perform(patch("/api/rental-units/1/block")
                        .param("isBlocked", "true")
                        .header("Authorization", "Bearer " + tokenFor(usuario)))
                .andExpect(status().isForbidden());
    }
}
