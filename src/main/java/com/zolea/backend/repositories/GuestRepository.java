package com.zolea.backend.repositories;

import com.zolea.backend.models.Guest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GuestRepository extends JpaRepository<Guest, Long> {

    Optional<Guest> findByQrToken(String qrToken);

    @Query("SELECT g FROM Guest g WHERE (g.dni = :dni OR g.booking.walkInDni = :dni) AND g.booking.status = 'CONFIRMED'")
    List<Guest> findConfirmedByDni(@Param("dni") String dni);
}