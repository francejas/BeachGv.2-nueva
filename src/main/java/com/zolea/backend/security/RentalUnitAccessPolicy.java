package com.zolea.backend.security;

import com.zolea.backend.repositories.RentalUnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Único punto donde vive la regla «esta unidad es del balneario de este administrador».
 *
 * <p>Se consulta desde las anotaciones de los endpoints con
 * {@code @unitAccess.belongsToResort(#id, principal.idManagedResort)}. Antes no existía: cualquier
 * administrador podía bloquear o re-preciar cualquiera de las cien unidades del sistema, porque
 * ninguna operación miraba de qué balneario era la unidad.
 */
@Component("unitAccess")
@RequiredArgsConstructor
public class RentalUnitAccessPolicy {

    private final RentalUnitRepository rentalUnitRepository;

    /**
     * Si esa unidad pertenece a ese balneario.
     *
     * <p>Sigue el mismo criterio que {@link BookingAccessPolicy#canAccess}: una unidad que no existe
     * se deja pasar para que el endpoint devuelva su 404, así el código de estado significa siempre
     * lo mismo. Un administrador sin balneario asignado ({@code idResort} nulo) no alcanza ninguna.
     */
    @Transactional(readOnly = true)
    public boolean belongsToResort(Long idRentalUnit, Long idResort) {
        if (idRentalUnit == null) {
            return false;
        }
        if (idResort == null) {
            return false;
        }
        return rentalUnitRepository.findById(idRentalUnit)
                .map(unit -> unit.getResort() != null
                        && idResort.equals(unit.getResort().getIdResort()))
                .orElse(true); // no existe: que el endpoint devuelva 404
    }
}
