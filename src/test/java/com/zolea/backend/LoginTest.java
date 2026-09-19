package com.zolea.backend;

import com.zolea.backend.models.Client;
import com.zolea.backend.support.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * El inicio de sesión no cuenta más de lo que debe.
 *
 * <p>El login respondía <b>distinto</b> según qué estuviera mal: «no existe una cuenta con ese
 * email» con un código, y «contraseña incorrecta» con otro. Eso convierte al endpoint en un
 * buscador de cuentas: probando direcciones se puede averiguar cuáles están registradas, sin
 * necesidad de acertar ninguna contraseña. Y el «no existe» salía además como <b>404</b>, que
 * semánticamente está mal: el recurso que se pidió —el endpoint de login— existe perfectamente.
 *
 * <p>Es el mismo tipo de falla que los accesos indebidos del bloque A: una respuesta que revela
 * algo que el que pregunta no debería poder averiguar.
 */
@AutoConfigureMockMvc
@DisplayName("Login — la respuesta no revela quién está registrado")
class LoginTest extends IntegrationTest {

    private static final String PASSWORD = "clave-de-prueba-12345";

    @Autowired private MockMvc mockMvc;
    @Autowired private PasswordEncoder passwordEncoder;

    /** Un cliente cuya contraseña realmente se puede verificar, no el hash falso de la fixture. */
    private Client aClientThatCanLogIn() {
        Client client = aClient();
        client.setPasswordHash(passwordEncoder.encode(PASSWORD));
        return clientRepository.save(client);
    }

    private String body(String email, String clave) {
        return """
                {"email":"%s","password":"%s"}
                """.formatted(email, clave);
    }

    private String failedLoginResponse(String email, String clave) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(email, clave)))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    @DisplayName("con las credenciales correctas devuelve el token y el id del cliente")
    void validLogin_returnsTokenAndClient() throws Exception {
        Client client = aClientThatCanLogIn();
        syncWithDatabase();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(client.getEmail(), PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.clientId").value(client.getIdClient()));
    }

    @Test
    @DisplayName("un email que no existe devuelve 401, no 404")
    void unknownEmail_returns401() throws Exception {
        syncWithDatabase();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("no.existe.nadie@test.zolea", PASSWORD)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("una contraseña incorrecta devuelve 401")
    void wrongPassword_returns401() throws Exception {
        Client client = aClientThatCanLogIn();
        syncWithDatabase();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(client.getEmail(), "no-es-la-clave")))
                .andExpect(status().isUnauthorized());
    }

    /**
     * La prueba que de verdad cierra el agujero.
     *
     * <p>Que los dos casos devuelvan 401 no alcanza si el cuerpo de la respuesta los distingue:
     * el que prueba direcciones lee el mensaje igual que el código.
     */
    @Test
    @DisplayName("el error es idéntico exista o no la cuenta")
    void theErrorDoesNotRevealWhetherTheAccountExists() throws Exception {
        Client client = aClientThatCanLogIn();
        syncWithDatabase();

        String cuentaQueExiste = failedLoginResponse(client.getEmail(), "no-es-la-clave");
        String cuentaQueNoExiste = failedLoginResponse("no.existe.nadie@test.zolea", "no-es-la-clave");

        assertThat(cuentaQueExiste)
                .withFailMessage("""
                        El login distingue una cuenta que existe de una que no:
                          existe    -> %s
                          no existe -> %s
                        Con eso se puede averiguar qué direcciones están registradas.""",
                        cuentaQueExiste, cuentaQueNoExiste)
                .isEqualTo(cuentaQueNoExiste);
    }
}
