package com.zolea.backend;

import com.zolea.backend.models.Client;
import com.zolea.backend.models.Resort;
import com.zolea.backend.security.JwtUtil;
import com.zolea.backend.security.UserPrincipal;
import com.zolea.backend.support.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * El frontend puede saber quién está usando la aplicación.
 *
 * <p>Hasta ahora el login devolvía el token y el id, nada más. Para saber si tiene que mostrar el
 * panel de administración, el frontend tenía dos opciones, las dos malas: <b>abrir el token por
 * dentro</b> y leerle el rol, o pedir un endpoint de administrador y deducirlo del código de error
 * que recibiera. Y al recargar la página con el token ya guardado no tenía forma de recuperar el
 * nombre de quien había iniciado sesión.
 *
 * <p>Abrir el token del lado del cliente es lo peor de las dos: acostumbra a tratar su contenido
 * como un dato confiable, cuando el token es algo que el servidor firma para sí mismo. Lo que el
 * cliente necesita saber, se lo tiene que contestar el servidor.
 */
@AutoConfigureMockMvc
@DisplayName("Sesión — el frontend sabe quién está conectado")
class CurrentUserTest extends IntegrationTest {

    private static final String PASSWORD = "clave-de-prueba-12345";

    @Autowired private MockMvc mockMvc;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;

    private Client aClientThatCanLogIn() {
        Client client = aClient();
        client.setPasswordHash(passwordEncoder.encode(PASSWORD));
        return clientRepository.save(client);
    }

    private String tokenFor(Client client) {
        return jwtUtil.generateToken(UserPrincipal.de(client));
    }

    // ---------------------------------------------------------------------

    @Test
    @DisplayName("el login dice con qué rol entró")
    void login_saysWhichRole() throws Exception {
        Client client = aClientThatCanLogIn();
        syncWithDatabase();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(client.getEmail(), PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @DisplayName("con el token, devuelve quién es el usuario")
    void me_returnsWhoIsLoggedIn() throws Exception {
        Client client = aClient();
        syncWithDatabase();

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + tokenFor(client)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idClient").value(client.getIdClient()))
                .andExpect(jsonPath("$.email").value(client.getEmail()))
                .andExpect(jsonPath("$.firstName").value(client.getFirstName()))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @DisplayName("de un administrador, dice además cuál es su balneario")
    void me_ofAnAdmin_includesTheirResort() throws Exception {
        Resort resort = aResort();
        Client admin = anAdminOf(resort);
        syncWithDatabase();

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + tokenFor(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.idManagedResort").value(resort.getIdResort()));
    }

    @Test
    @DisplayName("sin token responde 401, no los datos de nadie")
    void me_withoutToken_isRejected() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}
