ALTER TABLE refunds ADD COLUMN provider_ref VARCHAR(255);
CREATE INDEX idx_refunds_provider_ref ON refunds(provider_ref);
