package org.istiaqfuad.eventhub.ticket.repository;

import org.istiaqfuad.eventhub.ticket.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Optional<Ticket> findByPublicId(UUID publicId);
    List<Ticket> findByBookingId(Long bookingId);
    Optional<Ticket> findByBookingItemId(Long bookingItemId);
}
