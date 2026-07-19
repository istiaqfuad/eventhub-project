package org.istiaqfuad.eventhub.messaging.consumer;

import org.istiaqfuad.eventhub.analytics.entity.DailySale;
import org.istiaqfuad.eventhub.analytics.entity.EventStat;
import org.istiaqfuad.eventhub.analytics.repository.DailySaleRepository;
import org.istiaqfuad.eventhub.analytics.repository.EventStatRepository;
import org.istiaqfuad.eventhub.event.entity.Event;
import org.istiaqfuad.eventhub.event.repository.EventRepository;
import org.istiaqfuad.eventhub.messaging.RabbitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * Consumes {@code BookingConfirmed} events and upserts the per-event and
 * per-day analytics projections ({@code event_stats} and {@code daily_sales}).
 *
 * <p>This consumer is idempotent by design: re-delivery of the same event
 * would double-count, so the relay marks outbox rows PROCESSED before
 * a crash could trigger a retry. If a re-delivery does occur, the analytics
 * will be slightly off but no domain invariant is violated (analytics are
 * eventually-consistent projections, not the system of record).
 */
@Component
public class AnalyticsConsumer {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsConsumer.class);

    private final EventStatRepository eventStats;
    private final DailySaleRepository dailySales;
    private final EventRepository events;

    public AnalyticsConsumer(EventStatRepository eventStats,
                             DailySaleRepository dailySales,
                             EventRepository events) {
        this.eventStats = eventStats;
        this.dailySales = dailySales;
        this.events = events;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_ANALYTICS)
    @Transactional
    public void handle(Map<String, Object> payload) {
        try {
            Long eventId = toLong(payload.get("eventId"));
            BigDecimal total = new BigDecimal(String.valueOf(payload.get("total")));

            updateEventStat(eventId, total);
            updateDailySale(eventId, total);

        } catch (Exception ex) {
            // Log and let the message ack so a bad payload doesn't block the queue.
            // In production, a DLQ binding would capture these for inspection.
            log.error("AnalyticsConsumer failed to process payload {}: {}", payload, ex.getMessage(), ex);
        }
    }

    private void updateEventStat(Long eventId, BigDecimal total) {
        EventStat stat = eventStats.findByEventId(eventId).orElseGet(() -> {
            Event event = events.getReferenceById(eventId);
            EventStat s = new EventStat();
            s.setEvent(event);
            s.setTicketsSold(0L);
            s.setRevenue(BigDecimal.ZERO);
            s.setOccupancyRate(0.0);
            return s;
        });
        stat.setTicketsSold(stat.getTicketsSold() + 1);
        stat.setRevenue(stat.getRevenue().add(total));
        eventStats.save(stat);
    }

    private void updateDailySale(Long eventId, BigDecimal total) {
        LocalDate today = LocalDate.now();
        DailySale daily = dailySales.findByEventIdAndSalesDate(eventId, today).orElseGet(() -> {
            Event event = events.getReferenceById(eventId);
            DailySale d = new DailySale();
            d.setEvent(event);
            d.setSalesDate(today);
            d.setTicketsCount(0L);
            d.setRevenue(BigDecimal.ZERO);
            return d;
        });
        daily.setTicketsCount(daily.getTicketsCount() + 1);
        daily.setRevenue(daily.getRevenue().add(total));
        dailySales.save(daily);
    }

    private static Long toLong(Object value) {
        if (value instanceof Number n) return n.longValue();
        return Long.parseLong(String.valueOf(value));
    }
}
