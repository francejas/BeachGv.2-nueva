package com.zolea.backend.dtos.auth;

/**
 * Lo que devuelve iniciar sesión.
 *
 * <p>El {@code role} viaja acá para que el frontend no tenga que abrir el token por dentro para
 * saber qué mostrar. El token es algo que el servidor firma para sí mismo: que el cliente lo lea
 * funciona, pero acostumbra a tratar su contenido como un dato propio. Lo que el cliente necesita
 * saber, se lo contesta el servidor.
 *
 * <p>Conviene no confundir los dos usos: este rol sirve para <b>decidir qué dibujar</b>. Los
 * permisos de verdad los decide el backend en cada pedido, y los lee de la base, no del token.
 *
 * @param token    el JWT, que va en la cabecera {@code Authorization: Bearer ...}. Dura una hora.
 * @param clientId el identificador del usuario que inició sesión.
 * @param role     {@code USER} o {@code ADMIN}.
 */
public record AuthResponse(
        String token,
        Long clientId,
        String role
) {
}
