package org.istiaqfuad.eventhub.analytics.repository;

import org.istiaqfuad.eventhub.analytics.entity.DailySale;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DailySaleRepository extends JpaRepository<DailySale, Long> {

    Optional<DailySale> findByEventIdAndSalesDate(Long eventId, LocalDate salesDate);
}
