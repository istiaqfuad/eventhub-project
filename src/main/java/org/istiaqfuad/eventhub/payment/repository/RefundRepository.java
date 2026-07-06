package org.istiaqfuad.eventhub.payment.repository;

import org.istiaqfuad.eventhub.payment.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import java.util.Optional;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {
    List<Refund> findByPaymentId(Long paymentId);
    Optional<Refund> findByProviderRef(String providerRef);
}
