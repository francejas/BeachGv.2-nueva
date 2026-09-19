package com.zolea.backend.dtos.rentalunit;

import com.zolea.backend.models.UnitType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;

public record RentalUnitRequest(
        @Schema(description = "TENT para carpa, UMBRELLA para sombrilla", example = "TENT")
        @NotNull(message = "Hay que indicar si es carpa o sombrilla")
        UnitType type,

        @Schema(description = "Cómo se la llama en el balneario. No puede repetirse dentro del mismo balneario", example = "C-01")
        @NotBlank(message = "La unidad necesita un identificador")
        String identifier,

        @Schema(description = "Precio por día, con dos decimales", example = "12000.00")
        @NotNull(message = "El precio diario es obligatorio")
        @Positive(message = "El precio diario tiene que ser mayor que cero")
        BigDecimal dailyPrice,

        @Schema(description = "Una unidad bloqueada no se puede reservar, ni desde la web ni desde acá", example = "false")
        Boolean isBlocked,

        @Schema(description = "El balneario al que pertenece. Tiene que ser el del administrador que hace el pedido", example = "1")
        @NotNull(message = "Hay que indicar a qué balneario pertenece")
        @Positive(message = "El identificador del balneario no es válido")
        Long resortId
) {
}
