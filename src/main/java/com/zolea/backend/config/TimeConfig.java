package com.zolea.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Reloj de la aplicación.
 *
 * <p>Hasta ahora la lógica temporal llamaba directamente a {@code LocalDate.now()}, lo que traía
 * dos problemas. El primero es la zona horaria: ese método usa la zona de la JVM, y el contenedor
 * corre en UTC mientras el balneario opera en horario argentino (UTC-3), así que la ventana de
 * ingreso de una reserva podía correrse tres horas. El segundo es que no había forma de probar la
 * lógica de fechas de manera determinista, porque el resultado dependía del día en que se
 * ejecutara la prueba.
 *
 * <p>Al inyectar un {@link Clock} las dos cosas se resuelven: en producción es el reloj real
 * anclado a la zona del balneario, y una prueba puede sustituirlo por {@link Clock#fixed} para
 * situarse en un instante concreto.
 */
@Configuration
public class TimeConfig {

    /** Zona en la que opera el balneario. Todas las fechas del dominio se interpretan acá. */
    public static final ZoneId RESORT_ZONE = ZoneId.of("America/Argentina/Buenos_Aires");

    @Bean
    public Clock clock() {
        return Clock.system(RESORT_ZONE);
    }
}
