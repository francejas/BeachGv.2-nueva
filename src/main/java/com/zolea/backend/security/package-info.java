/**
 * Autenticación con JWT y autorización por dueño y por rol.
 *
 * <p>El filtro {@code JwtAuthenticationFilter} valida el token y deja el contexto de seguridad
 * armado con un {@link com.zolea.backend.security.UserPrincipal}, que <b>transporta el id del
 * cliente</b>. Sin ese id no hay con qué responder «¿esta reserva es tuya?», que es la pregunta que
 * ningún endpoint hacía.
 *
 * <p>Las políticas ({@code BookingAccessPolicy}, {@code RentalUnitAccessPolicy}) son el único lugar
 * donde vive esa pregunta, y las consultan las anotaciones {@code @PreAuthorize} de los controllers.
 *
 * <p>La regla por omisión de {@code SecurityConfig} es {@code denyAll()}: un endpoint nuevo al que se
 * le olvide su anotación nace <b>cerrado</b>. Con {@code authenticated()}, que es lo que había antes,
 * ese olvido lo dejaba abierto a cualquier usuario con sesión.
 */
package com.zolea.backend.security;
