-- ============================================================================
-- V4: general-admission booking items + one-review-per-user-per-event
-- ============================================================================

-- (1) General admission: a booking item references an assigned seat OR a
-- ticket type (GA), not necessarily a seat. Existing uq_booking_item_seat
-- UNIQUE (seat_id) stays valid: Postgres treats NULLs as distinct, so multiple
-- GA rows (seat_id NULL) coexist; assigned seats remain single-sell.
ALTER TABLE booking_items ALTER COLUMN seat_id DROP NOT NULL;

ALTER TABLE booking_items
    ADD CONSTRAINT ck_booking_item_target
    CHECK (seat_id IS NOT NULL OR ticket_type_id IS NOT NULL);

-- (2) One review per (event, user).
ALTER TABLE reviews
    ADD CONSTRAINT uq_review_event_user UNIQUE (event_id, user_id);
