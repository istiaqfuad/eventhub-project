package org.istiaqfuad.eventhub.payment.repository;

import org.istiaqfuad.eventhub.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByBookingId(Long bookingId);

    Optional<Payment> findByProviderRef(String providerRef);
}
