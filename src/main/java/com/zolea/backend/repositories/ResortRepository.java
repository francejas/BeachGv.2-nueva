package com.zolea.backend.repositories;


import com.zolea.backend.models.Resort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResortRepository extends JpaRepository<Resort,Long> {
    Optional<Resort> findByName(String name);
    Optional<Resort> findByAdminEmail(String adminEmail);

    /** Los balnearios que hoy se muestran al público. Un balneario desactivado no se lista. */
    List<Resort> findByIsActiveTrue();

    /**
     * Los balnearios que ofrecen una amenidad. Se usa al borrarla, para desvincularla primero:
     * borrar una fila a la que otra tabla apunta falla con un error de integridad.
     */
    List<Resort> findByAmenities_IdAmenity(Long idAmenity);
}

