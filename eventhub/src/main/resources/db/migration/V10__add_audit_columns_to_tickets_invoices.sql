ALTER TABLE tickets
    ADD COLUMN created_by BIGINT,
    ADD COLUMN updated_by BIGINT;

ALTER TABLE invoices
    ADD COLUMN created_by BIGINT,
    ADD COLUMN updated_by BIGINT;
