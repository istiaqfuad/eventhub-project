-- ============================================================================
-- V11: missing indexes for the polling hot paths
-- ============================================================================

-- AdmissionWorker: findByHighDemandTrue() was a sequential scan on events
-- (the only index, idx_event_status_starts, doesn't cover high_demand).
-- Partial index: only index the rare rows where high_demand = true.
CREATE INDEX idx_events_high_demand
    ON events (high_demand) WHERE high_demand = true;

-- OutboxRelay: the existing idx_outbox_unprocessed filters on
-- processed_at IS NULL, but the query filters on status = 'PENDING'.
-- Postgres can't use a partial index whose predicate doesn't match the
-- query, so the relay was seq-scanning outbox_events every idle cycle.
CREATE INDEX idx_outbox_pending
    ON outbox_events (created_at) WHERE status = 'PENDING';
