package com.zolea.backend;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.zolea.backend.exceptions.GlobalExceptionHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Que un error inesperado deje rastro.
 *
 * <p>El manejador genérico devuelve «Error interno del servidor» y descarta la excepción, así que
 * cuando algo falla en vivo no hay dónde mirar: ni el stacktrace, ni el mensaje original. Esta
 * prueba fija lo mínimo indispensable, que es que el fallo se registre en nivel ERROR y con la
 * excepción adjunta —no solo su mensaje—, porque sin la causa el registro no sirve para diagnosticar.
 *
 * <p>Es una prueba de unidad: el manejador es una clase común y no necesita levantar Spring.
 */
@DisplayName("Manejador de errores — observabilidad")
class ExceptionHandlerTest {

    private ListAppender<ILoggingEvent> registro;
    private Logger logger;

    @BeforeEach
    void captureTheLog() {
        logger = (Logger) LoggerFactory.getLogger(GlobalExceptionHandler.class);
        registro = new ListAppender<>();
        registro.start();
        logger.addAppender(registro);
    }

    @AfterEach
    void releaseTheLog() {
        logger.detachAppender(registro);
        registro.stop();
    }

    @Test
    @DisplayName("un error inesperado se registra en nivel ERROR y con su causa")
    void unexpectedError_isLoggedWithItsCause() {
        RuntimeException falloReal = new RuntimeException("la base se cayó");

        new GlobalExceptionHandler().handleGeneric(falloReal);

        assertThat(registro.list)
                .withFailMessage("el manejador genérico no registró nada")
                .hasSize(1);
        assertThat(registro.list.getFirst().getLevel()).isEqualTo(Level.ERROR);
        assertThat(registro.list.getFirst().getThrowableProxy())
                .withFailMessage("se registró el mensaje pero se perdió la excepción")
                .isNotNull();
    }

    @Test
    @DisplayName("la respuesta al cliente sigue sin filtrar el detalle interno")
    void unexpectedError_doesNotLeakDetailToClient() {
        var respuesta = new GlobalExceptionHandler()
                .handleGeneric(new RuntimeException("SELECT * FROM client falló"));

        assertThat(respuesta.getStatusCode().value()).isEqualTo(500);
        assertThat(respuesta.getBody()).containsExactly(
                java.util.Map.entry("error", "Error interno del servidor"));
    }
}
