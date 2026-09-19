package com.zolea.backend;

import com.zolea.backend.models.Client;
import com.zolea.backend.models.RentalUnit;
import com.zolea.backend.models.Resort;
import com.zolea.backend.security.JwtUtil;
import com.zolea.backend.security.UserPrincipal;
import com.zolea.backend.support.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Lo que entra por la API se valida en la API.
 *
 * <p>El proyecto no tenía <b>ninguna</b> validación de entrada: ni la librería estaba en el
 * {@code pom.xml}. La única que existía vivía en React y en dos {@code if} escritos a mano, así que
 * cualquier petición que no viniera de la propia web —Swagger, curl, un cliente mal programado—
 * entraba sin control.
 *
 * <p><b>Dónde vive cada regla, para no tener dos fuentes de verdad:</b> la validación de entrada
 * cubre la <em>forma</em> del pedido —qué campos son obligatorios, qué números tienen que ser
 * positivos, qué texto no puede venir vacío—. Las reglas de negocio —que el fin no sea anterior al
 * inicio, que la unidad no esté bloqueada, que la reserva no esté cancelada— viven en las entidades
 * y se prueban en {@link DomainRulesTest}.
 */
@AutoConfigureMockMvc
@DisplayName("Validación de entrada — la forma del pedido")
class InputValidationTest extends IntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtUtil jwtUtil;

    private String tokenFor(Client client) {
        return jwtUtil.generateToken(UserPrincipal.de(client));
    }

    // ---------------------------------------------------------------------
    // C.9 — Bean Validation
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("registrarse sin email se rechaza con 400")
    void signUpWithoutEmail_rejected() throws Exception {
        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Sin","lastName":"Email","email":"",
                                 "password":"clave-larga-de-prueba","phone":"1155000111","dni":"38000111"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("registrarse con un email que no es un email se rechaza con 400")
    void signUpWithInvalidEmail_rejected() throws Exception {
        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Mal","lastName":"Email","email":"esto-no-es-un-email",
                                 "password":"clave-larga-de-prueba","phone":"1155000222","dni":"38000222"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("la respuesta de una validación fallida dice qué campo está mal")
    void failedValidation_namesTheField() throws Exception {
        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"","lastName":"Sin Nombre","email":"ok@test.zolea",
                                 "password":"clave-larga-de-prueba","phone":"1155000333","dni":"38000333"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.campos.firstName").exists());
    }

    @Test
    @DisplayName("reservar sin fecha de inicio se rechaza con 400, sin llegar a la pasarela de pago")
    void bookingWithoutDate_rejected() throws Exception {
        Client client = aClient();
        RentalUnit unit = aUnit(5_000.0);
        syncWithDatabase();

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + tokenFor(client))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"startDate":null,"endDate":"%s","clientId":%d,"rentalUnitId":%d,"guests":[]}
                                """.formatted(today().plusDays(12), client.getIdClient(),
                                unit.getIdRentalUnit())))
                .andExpect(status().isBadRequest());
    }

    /**
     * La trampa que la auditoría marcó por escrito.
     *
     * <p>Un {@code @NotNull @Positive} sobre {@code clientId} sería lo natural, y rompería la
     * reserva de mostrador: esa llega con {@code clientId} en cero y el titular en
     * {@code walkInName}. Por eso la regla «tiene que haber un titular» es una restricción de la
     * clase entera y no de un campo.
     */
    @Test
    @DisplayName("una reserva sin titular —ni cliente ni nombre de mostrador— se rechaza con 400")
    void bookingWithoutHolder_rejected() throws Exception {
        Client admin = anAdmin();
        RentalUnit unit = aUnit(5_000.0);
        syncWithDatabase();

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + tokenFor(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"startDate":"%s","endDate":"%s","clientId":0,"rentalUnitId":%d,
                                 "guests":[],"walkInName":null,"walkInDni":null}
                                """.formatted(today().plusDays(10), today().plusDays(12),
                                unit.getIdRentalUnit())))
                .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------------------
    // C.10 — el precio negativo
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("poner un precio negativo se rechaza y no toca la unidad")
    void negativePrice_rejected() throws Exception {
        Resort resort = aResort();
        RentalUnit unidad = aUnitIn(resort, 5_000.0);
        Client admin = anAdminOf(resort);
        syncWithDatabase();

        mockMvc.perform(patch("/api/rental-units/" + unidad.getIdRentalUnit() + "/price")
                        .param("newPrice", "-5000")
                        .header("Authorization", "Bearer " + tokenFor(admin)))
                .andExpect(status().isBadRequest());

        syncWithDatabase();
        assertThat(rentalUnitRepository.findById(unidad.getIdRentalUnit()).orElseThrow()
                .getDailyPrice()).isEqualByComparingTo("5000.00");
    }

    @Test
    @DisplayName("crear una unidad con precio negativo se rechaza")
    void unitWithNegativePrice_rejected() throws Exception {
        Resort resort = aResort();
        Client admin = anAdminOf(resort);
        syncWithDatabase();

        mockMvc.perform(post("/api/rental-units")
                        .header("Authorization", "Bearer " + tokenFor(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type":"TENT","identifier":"NEG-1","dailyPrice":-100,
                                 "isBlocked":false,"resortId":%d}
                                """.formatted(resort.getIdResort())))
                .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------------------
    // C.11 — la unicidad, en la base y no solo en el código
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("la base rechaza dos clientes con el mismo teléfono")
    void duplicatePhone_rejectedByDatabase() {
        Client primero = aClient();
        syncWithDatabase();

        Client segundo = new Client();
        segundo.setFirstName("Otro");
        segundo.setLastName("Cliente");
        segundo.setEmail("otro.telefono@test.zolea");
        segundo.setPasswordHash("$2b$10$hash.de.prueba.no.se.verifica.nunca.aaaaaaaaaaaaaaaaaaaaaa");
        segundo.setPhone(primero.getPhone()); // el mismo teléfono
        segundo.setDni("39000111");
        segundo.setRole("USER");

        // El servicio ya lo chequeaba antes de guardar, pero un chequeo previo no es una garantía:
        // dos peticiones simultáneas pasan las dos. La garantía es la restricción de la base.
        assertThatThrownBy(() -> {
            clientRepository.save(segundo);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("dos unidades con el mismo identificador en el mismo balneario se rechazan")
    void duplicateIdentifierInResort_rejected() throws Exception {
        Resort resort = aResort();
        Client admin = anAdminOf(resort);
        syncWithDatabase();

        String cuerpo = """
                {"type":"TENT","identifier":"REPE-1","dailyPrice":9000,
                 "isBlocked":false,"resortId":%d}
                """.formatted(resort.getIdResort());

        mockMvc.perform(post("/api/rental-units")
                        .header("Authorization", "Bearer " + tokenFor(admin))
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isCreated());
        syncWithDatabase();

        mockMvc.perform(post("/api/rental-units")
                        .header("Authorization", "Bearer " + tokenFor(admin))
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------------------
    // C.12 — el PUT parcial que borraba lo que no venía
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("actualizar un balneario sin mandar la foto no la borra")
    void partialResortUpdate_keepsOmittedFields() throws Exception {
        Resort resort = aResort();
        resort.setCoverPhotoUrl("https://ejemplo.test/portada.jpg");
        resort.setDescription("Descripción original del balneario.");
        resortRepository.save(resort);
        Client admin = anAdminOf(resort);
        syncWithDatabase();

        mockMvc.perform(put("/api/resorts/" + resort.getIdResort())
                        .header("Authorization", "Bearer " + tokenFor(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Nombre Nuevo","location":"Pinamar","adminEmail":"%s"}
                                """.formatted(resort.getAdminEmail())))
                .andExpect(status().isOk());

        syncWithDatabase();
        Resort guardado = resortRepository.findById(resort.getIdResort()).orElseThrow();
        assertThat(guardado.getName()).isEqualTo("Nombre Nuevo");
        assertThat(guardado.getCoverPhotoUrl())
                .withFailMessage("un PUT sin foto dejó la portada pública en null")
                .isEqualTo("https://ejemplo.test/portada.jpg");
        assertThat(guardado.getDescription()).isEqualTo("Descripción original del balneario.");
    }
}
