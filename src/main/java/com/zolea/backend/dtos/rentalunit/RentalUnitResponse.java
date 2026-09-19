package com.zolea.backend.dtos.rentalunit;
import com.zolea.backend.models.UnitType;

import java.math.BigDecimal;
public record RentalUnitResponse(
        Long idRentalUnit,
        UnitType type,
        String identifier,
        BigDecimal dailyPrice,
        Boolean isBlocked,
        // Sin esto no se puede saber de qué balneario es una unidad, así que ningún consumidor
        // podía agrupar ni filtrar por balneario aunque quisiera.
        Long resortId
) {
}
