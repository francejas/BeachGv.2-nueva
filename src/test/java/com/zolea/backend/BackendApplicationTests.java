package com.zolea.backend;

import com.zolea.backend.support.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Verifica que el contexto de Spring levante entero.
 *
 * <p>Antes esta prueba no se podía ejecutar: no declaraba perfil, así que pedía el MySQL de
 * desarrollo y las variables de entorno de MercadoPago y del JWT. En un clone limpio fallaba con
 * {@code Could not resolve placeholder}. Ahora corre bajo el perfil {@code test}, que trae sus
 * propios valores.
 */
@DisplayName("Arranque de la aplicación")
class BackendApplicationTests extends IntegrationTest {

    @Test
    @DisplayName("el contexto de Spring se levanta completo")
    void contextLoads() {
    }
}
