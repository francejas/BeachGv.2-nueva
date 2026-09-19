package com.zolea.backend.dtos.auth;

import io.swagger.v3.oas.annotations.media.Schema;

public record AuthRequest(
        @Schema(description = "Email de la cuenta", example = "admin.mdp@zolea.com")
        String email,
        @Schema(description = "Contraseña", example = "admin123")
        String password
) {
}
