package com.zolea.backend.dtos.resort;

/**
 * El resultado de una operación de administración sobre un balneario: crearlo, editarlo, activarlo
 * o desactivarlo.
 *
 * <p>Las cuatro devuelven lo mismo —un mensaje y el balneario como quedó— y por eso comparten un
 * solo tipo en vez de tener uno cada una. El balneario viaja en la respuesta a propósito: evita que
 * el panel tenga que volver a pedirlo para refrescar la pantalla.
 *
 * @param message texto para mostrarle al administrador.
 * @param resort  el balneario después del cambio.
 */
public record ResortActionResponse(
        String message,
        ResortResponse resort
) {
}
