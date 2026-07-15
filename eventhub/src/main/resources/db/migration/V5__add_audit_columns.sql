-- ---------------------------------------------------------------------------
-- Actor attribution (who created / last modified a row).
-- Nullable so existing rows and system/anonymous actions remain valid.
-- FK -> users(id) with ON DELETE SET NULL keeps audit values sane if an actor
-- user is ever removed. Populated later once Spring Security supplies a principal.
-- ---------------------------------------------------------------------------
ALTER TABLE users
    ADD COLUMN created_by BIGINT REFERENCES users (id) ON DELETE SET NULL,
    ADD COLUMN updated_by BIGINT REFERENCES users (id) ON DELETE SET NULL;

ALTER TABLE organizers
    ADD COLUMN created_by BIGINT REFERENCES users (id) ON DELETE SET NULL,
    ADD COLUMN updated_by BIGINT REFERENCES users (id) ON DELETE SET NULL;

ALTER TABLE venues
    ADD COLUMN created_by BIGINT REFERENCES users (id) ON DELETE SET NULL,
    ADD COLUMN updated_by BIGINT REFERENCES users (id) ON DELETE SET NULL;

ALTER TABLE events
    ADD COLUMN created_by BIGINT REFERENCES users (id) ON DELETE SET NULL,
    ADD COLUMN updated_by BIGINT REFERENCES users (id) ON DELETE SET NULL;

ALTER TABLE ticket_types
    ADD COLUMN created_by BIGINT REFERENCES users (id) ON DELETE SET NULL,
    ADD COLUMN updated_by BIGINT REFERENCES users (id) ON DELETE SET NULL;

ALTER TABLE bookings
    ADD COLUMN created_by BIGINT REFERENCES users (id) ON DELETE SET NULL,
    ADD COLUMN updated_by BIGINT REFERENCES users (id) ON DELETE SET NULL;

ALTER TABLE payments
    ADD COLUMN created_by BIGINT REFERENCES users (id) ON DELETE SET NULL,
    ADD COLUMN updated_by BIGINT REFERENCES users (id) ON DELETE SET NULL;

ALTER TABLE refunds
    ADD COLUMN created_by BIGINT REFERENCES users (id) ON DELETE SET NULL,
    ADD COLUMN updated_by BIGINT REFERENCES users (id) ON DELETE SET NULL;

ALTER TABLE reviews
    ADD COLUMN created_by BIGINT REFERENCES users (id) ON DELETE SET NULL,
    ADD COLUMN updated_by BIGINT REFERENCES users (id) ON DELETE SET NULL;
