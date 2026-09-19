package com.zolea.backend.repositories;

import com.zolea.backend.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /** Si ya registramos este pago. Es la comprobación previa; la garantía es el índice único. */
    Optional<Payment> findByGatewayPaymentId(String gatewayPaymentId);

    boolean existsByGatewayPaymentId(String gatewayPaymentId);
}
