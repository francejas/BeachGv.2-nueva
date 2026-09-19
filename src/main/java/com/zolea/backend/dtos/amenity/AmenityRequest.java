package com.zolea.backend.dtos.amenity;

import io.swagger.v3.oas.annotations.media.Schema;

public record AmenityRequest (
        @Schema(description = "Nombre del servicio", example = "Estacionamiento")
        String name
) {
}
