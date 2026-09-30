package org.istiaqfuad.eventhub.outbox.service;

import org.istiaqfuad.eventhub.outbox.entity.OutboxStatus;
import org.istiaqfuad.eventhub.outbox.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * Purges processed outbox events older than the retention window.
 *
 * <p>The outbox table only ever grows; without a relay-side purge the idle poll
 * would scan an ever-larger table. Runs once a day at 03:17 (off the :00 mark to
 * avoid thundering-herd with other jobs).
 */
@Service
public class OutboxRetentionService {

    private static final Logger log = LoggerFactory.getLogger(OutboxRetentionService.class);

    /** Keep processed events for 7 days — enough for debugging replay. */
    private static final int RETENTION_DAYS = 7;

    private final OutboxEventRepository repository;

    public OutboxRetentionService(OutboxEventRepository repository) {
        this.repository = repository;
    }

    @Scheduled(cron = "0 17 3 * * *")
    @Transactional
    public int purge() {
        OffsetDateTime cutoff = OffsetDateTime.now().minusDays(RETENTION_DAYS);
        int deleted = repository.deleteByStatusInAndProcessedAtBefore(
                java.util.List.of(OutboxStatus.PROCESSED, OutboxStatus.FAILED), cutoff);
        if (deleted > 0) {
            log.info("Purged {} outbox events older than {} days", deleted, RETENTION_DAYS);
        }
        return deleted;
    }
}
