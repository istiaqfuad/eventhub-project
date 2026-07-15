package org.istiaqfuad.eventhub.event.repository;

import java.util.List;

import org.istiaqfuad.eventhub.event.entity.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {

    List<TicketType> findByEventId(Long eventId);

    /** Atomically reserves {@code n} units iff it stays within quota. Returns rows affected (1 = reserved, 0 = would oversell). */
    @Modifying
    @Query("update TicketType t set t.sold = t.sold + :n where t.id = :id and t.sold + :n <= t.quota")
    int reserve(@Param("id") Long id, @Param("n") int n);

    /** Returns {@code n} units to the pool, never below zero. Returns rows affected. */
    @Modifying
    @Query("update TicketType t set t.sold = t.sold - :n where t.id = :id and t.sold - :n >= 0")
    int release(@Param("id") Long id, @Param("n") int n);
}
