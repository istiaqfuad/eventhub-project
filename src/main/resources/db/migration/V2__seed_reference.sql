-- ============================================================================
-- V2: reference / seed data (idempotent inserts)
-- ============================================================================

INSERT INTO roles (name) VALUES
    ('CUSTOMER'),
    ('ORGANIZER'),
    ('ADMIN')
ON CONFLICT (name) DO NOTHING;

INSERT INTO categories (name, slug) VALUES
    ('Concerts',   'concerts'),
    ('Sports',     'sports'),
    ('Theater',    'theater'),
    ('Conference', 'conference'),
    ('Festival',   'festival')
ON CONFLICT (slug) DO NOTHING;
