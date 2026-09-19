/**
 * Configuración transversal de la aplicación.
 *
 * <p>Acá vive {@link com.zolea.backend.config.TimeConfig}, que publica el {@code Clock} anclado a
 * la hora argentina. Es más importante de lo que parece: el contenedor corre en UTC mientras el
 * balneario opera en UTC-3, así que sin él la ventana de ingreso de una reserva se corre tres horas.
 * Además hace deterministas las pruebas que dependen de fechas.
 *
 * <p><b>Regla del proyecto:</b> nada de {@code LocalDate.now()} sin argumento. Siempre
 * {@code LocalDate.now(clock)} con el reloj inyectado.
 */
package com.zolea.backend.config;
