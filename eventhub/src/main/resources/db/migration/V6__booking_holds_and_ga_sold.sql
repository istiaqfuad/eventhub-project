-- ============================================================================
-- V6: GA sold counter + booking hold expiry
-- ============================================================================

-- General-admission oversell guard: a running count reserved via an atomic
-- conditional UPDATE; the CHECK is the last-line backstop.
ALTER TABLE ticket_types
    ADD COLUMN sold INTEGER NOT NULL DEFAULT 0,
    ADD CONSTRAINT ck_ticket_type_sold CHECK (sold >= 0 AND sold <= quota);

-- Hold expiry: set while PENDING, cleared on confirm/cancel.
ALTER TABLE bookings
    ADD COLUMN expires_at TIMESTAMPTZ;

-- Sweeper hot path: PENDING bookings past their hold.
CREATE INDEX idx_booking_pending_expiry
    ON bookings (expires_at) WHERE status = 'PENDING';
