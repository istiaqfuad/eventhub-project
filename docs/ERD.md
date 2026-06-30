# EventHub — Entity Relationship Diagram

> Source of truth: Flyway migrations `V1`–`V4`. Mirrors the Postgres/Neon
> schema and the JPA entities under `org.istiaqfuad.eventhub.*`.
> Link tables `user_roles` / `event_tags` map to `@ManyToMany`;
> `event_images` maps to `@ElementCollection`.

```mermaid
erDiagram
    users ||--o{ user_roles : has
    roles ||--o{ user_roles : has
    users ||--o| organizers : "is a"
    users ||--o{ refresh_tokens : has
    users ||--o{ bookings : places
    users ||--o{ reviews : writes
    users ||--o{ notifications : receives

    venues ||--o{ sections : has
    sections ||--o{ seats : has
    organizers ||--o{ events : hosts
    categories ||--o{ events : groups
    venues ||--o{ events : hosts

    events ||--o{ event_tags : has
    tags ||--o{ event_tags : labels
    events ||--o{ event_images : has
    events ||--o{ ticket_types : offers
    events ||--o{ bookings : "booked in"
    events ||--o{ reviews : receives
    events ||--o| event_stats : "has stats"
    events ||--o{ daily_sales : "has sales"

    bookings ||--o{ booking_items : contains
    seats ||--o| booking_items : "sold as"
    ticket_types ||--o{ booking_items : "typed as"
    bookings ||--o{ payments : "paid by"
    payments ||--o{ refunds : "refunded by"

    users {
        bigint id PK
        varchar email UK
        varchar password_hash
        boolean enabled
        uuid public_id UK
        timestamptz created_at
        timestamptz updated_at
    }
    roles {
        bigint id PK
        varchar name UK
    }
    user_roles {
        bigint user_id PK,FK
        bigint role_id PK,FK
    }
    organizers {
        bigint id PK
        bigint user_id FK,UK
        varchar org_name
        boolean verified
        timestamptz created_at
        timestamptz updated_at
    }
    refresh_tokens {
        bigint id PK
        bigint user_id FK
        varchar token_hash UK
        timestamptz expires_at
        boolean revoked
        timestamptz created_at
        timestamptz updated_at
    }
    venues {
        bigint id PK
        varchar name
        varchar layout_type
        varchar address
        varchar city
        timestamptz created_at
        timestamptz updated_at
    }
    sections {
        bigint id PK
        bigint venue_id FK
        varchar name
        varchar seat_type
        numeric base_price
        timestamptz created_at
        timestamptz updated_at
    }
    seats {
        bigint id PK
        bigint section_id FK
        varchar row_label
        integer col_number
        varchar status
        bigint version
        timestamptz created_at
        timestamptz updated_at
    }
    categories {
        bigint id PK
        varchar name
        varchar slug UK
    }
    tags {
        bigint id PK
        varchar name UK
    }
    events {
        bigint id PK
        bigint organizer_id FK
        varchar title
        text description
        bigint category_id FK
        bigint venue_id FK
        varchar city
        double latitude
        double longitude
        timestamptz starts_at
        timestamptz ends_at
        varchar status
        boolean high_demand
        uuid public_id UK
        timestamptz created_at
        timestamptz updated_at
    }
    event_tags {
        bigint event_id PK,FK
        bigint tag_id PK,FK
    }
    event_images {
        bigint event_id FK
        varchar url
    }
    ticket_types {
        bigint id PK
        bigint event_id FK
        varchar name
        numeric price
        integer quota
        timestamptz created_at
        timestamptz updated_at
    }
    bookings {
        bigint id PK
        bigint user_id FK
        bigint event_id FK
        varchar status
        numeric total
        uuid public_id UK
        timestamptz created_at
        timestamptz updated_at
    }
    booking_items {
        bigint id PK
        bigint booking_id FK
        bigint seat_id FK,UK
        bigint ticket_type_id FK
        numeric price
        uuid public_id UK
        timestamptz created_at
        timestamptz updated_at
    }
    payments {
        bigint id PK
        bigint booking_id FK
        numeric amount
        varchar status
        varchar provider_ref
        varchar idempotency_key UK
        timestamptz created_at
        timestamptz updated_at
    }
    refunds {
        bigint id PK
        bigint payment_id FK
        numeric amount
        varchar status
        varchar reason
        timestamptz created_at
        timestamptz updated_at
    }
    reviews {
        bigint id PK
        bigint event_id FK
        bigint user_id FK
        integer rating
        text body
        timestamptz created_at
        timestamptz updated_at
    }
    notifications {
        bigint id PK
        bigint user_id FK
        varchar type
        jsonb payload
        boolean is_read
        timestamptz created_at
        timestamptz updated_at
    }
    event_stats {
        bigint id PK
        bigint event_id FK,UK
        bigint tickets_sold
        numeric revenue
        double occupancy_rate
        timestamptz created_at
        timestamptz updated_at
    }
    daily_sales {
        bigint id PK
        bigint event_id FK
        date sales_date
        bigint tickets_count
        numeric revenue
        timestamptz created_at
        timestamptz updated_at
    }
    outbox_events {
        uuid id PK
        varchar aggregate_type
        varchar aggregate_id
        varchar event_type
        jsonb payload
        varchar status
        timestamptz created_at
        timestamptz processed_at
    }
    idempotency_keys {
        varchar idem_key PK
        varchar request_hash
        jsonb response
        varchar status
        timestamptz created_at
        timestamptz expires_at
    }
```

## Composite / multi-column constraints

Not expressible in Mermaid attribute markers, but present in the schema:

- `seats` — `UNIQUE (section_id, row_label, col_number)`
- `booking_items` — `UNIQUE (seat_id)` (nullable → general-admission rows skip it); `CHECK (seat_id IS NOT NULL OR ticket_type_id IS NOT NULL)`
- `reviews` — `UNIQUE (event_id, user_id)` (V4)
- `daily_sales` — `UNIQUE (event_id, sales_date)`
- `user_roles` / `event_tags` — composite primary key (both FK columns)

## Standalone tables (reliability layer)

`outbox_events`, `idempotency_keys` — intentionally have no FKs to domain tables
(transactional outbox + idempotency dedup).

## Keeping this in sync

- **On schema change:** add a Flyway `V{n}` migration, then edit this file to
  match. `git diff` shows the delta — this file is the version-controlled sync
  record.
- **Live (auto-sync) diagrams in IntelliJ:**
  - Datasource ERD — Database tool → right-click schema → **Diagrams → Show
    Visualization** (`Ctrl+Alt+Shift+U`). Reflects the live Neon schema.
  - Entity ERD — **Persistence** tool window → right-click persistence unit →
    **Entity Relationship Diagram**. Reflects the `@Entity` classes.
