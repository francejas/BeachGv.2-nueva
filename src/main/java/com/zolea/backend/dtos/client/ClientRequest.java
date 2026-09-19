package com.zolea.backend.dtos.client;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Datos de un cliente, tanto para registrarlo como para actualizarlo.
 *
 * <p>La contraseña <b>no</b> lleva {@code @NotBlank} aunque al registrarse sea obligatoria: el mismo
 * DTO se usa para actualizar el perfil, donde venir vacía significa «no la cambies». Que esté
 * presente al registrarse lo comprueba {@code ClientService.registerClient}, que es quien conoce la
 * diferencia entre las dos operaciones.
 */
public record ClientRequest(
        @Schema(description = "Nombre", example = "Santiago")
        @NotBlank(message = "El nombre es obligatorio")
        String firstName,

        @Schema(description = "Apellido", example = "Rodríguez")
        @NotBlank(message = "El apellido es obligatorio")
        String lastName,

        @Schema(description = "Email, con el que después inicia sesión. No puede repetirse", example = "santi.rodriguez@gmail.com")
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato válido")
        String email,

        @Schema(description = "Contraseña. Obligatoria al registrarse; al editar el perfil, vacía significa «no la cambies»", example = "admin123")
        String password,

        @Schema(description = "Teléfono. No puede repetirse entre cuentas", example = "2235123456")
        @NotBlank(message = "El teléfono es obligatorio")
        String phone,

        @Schema(description = "DNI, sin puntos. Sirve para entrar al balneario si el cliente no tiene el código QR a mano", example = "40123456")
        String dni
) {
}
