package com.zolea.backend.dtos.auth;

/**
 * Quién está usando la aplicación ahora mismo.
 *
 * <p>Lo que contesta {@code GET /api/auth/me}. Existe para el caso de recargar la página: el
 * frontend tiene el token guardado pero perdió todo lo demás, y con una sola llamada recupera al
 * usuario en vez de pedirle que vuelva a iniciar sesión o de leerle el token por dentro.
 *
 * <p>No incluye la contraseña ni el teléfono ni el DNI: es lo mínimo para saludar al usuario y
 * decidir qué menú mostrarle. Los datos completos del perfil están en {@code GET /api/clients/id}.
 *
 * @param idClient        identificador del usuario.
 * @param firstName       nombre, para saludarlo.
 * @param lastName        apellido.
 * @param email           con el que inició sesión.
 * @param role            {@code USER} o {@code ADMIN}.
 * @param idManagedResort el balneario que administra, o {@code null} si no administra ninguno.
 */
public record CurrentUserResponse(
        Long idClient,
        String firstName,
        String lastName,
        String email,
        String role,
        Long idManagedResort
) {
}
