package org.istiaqfuad.eventhub.invoice.repository;

import org.istiaqfuad.eventhub.invoice.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByBookingId(Long bookingId);
}
