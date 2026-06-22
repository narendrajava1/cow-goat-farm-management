# 04 — Database Design

## 1. Database-per-Service Strategy

Each microservice owns its own database schema. No cross-schema joins — data needed across services is either replicated via events or fetched via API.

| Service | DB Type | Schema Name |
|---|---|---|
| Animal Service | PostgreSQL 16 | `farm_animals` |
| Health Service | PostgreSQL 16 | `farm_health` |
| Breeding Service | PostgreSQL 16 | `farm_breeding` |
| Milk Service | TimescaleDB | `farm_milk` |
| Inventory Service | PostgreSQL 16 | `farm_inventory` |
| Finance Service | PostgreSQL 16 | `farm_finance` |
| Notification Service | MongoDB 7 | `farm_notifications` |

---

## 2. Animal Service Schema (`farm_animals`)

```sql
-- Farms table
CREATE TABLE farms (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name          VARCHAR(255) NOT NULL,
    owner_name    VARCHAR(255),
    location      TEXT,
    contact_phone VARCHAR(20),
    contact_email VARCHAR(255),
    currency      VARCHAR(10) DEFAULT 'INR',
    timezone      VARCHAR(50) DEFAULT 'Asia/Kolkata',
    created_at    TIMESTAMPTZ DEFAULT NOW(),
    updated_at    TIMESTAMPTZ DEFAULT NOW()
);

-- Herds table
CREATE TABLE herds (
    id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    farm_id  UUID NOT NULL REFERENCES farms(id),
    name     VARCHAR(255) NOT NULL,
    type     VARCHAR(20) CHECK (type IN ('COW','GOAT','MIXED')),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Animals table (core)
CREATE TABLE animals (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    farm_id          UUID NOT NULL REFERENCES farms(id),
    herd_id          UUID REFERENCES herds(id),
    tag_number       VARCHAR(100) NOT NULL,
    name             VARCHAR(100),
    animal_type      VARCHAR(10) NOT NULL CHECK (animal_type IN ('COW','GOAT')),
    breed            VARCHAR(100),
    gender           VARCHAR(10) NOT NULL CHECK (gender IN ('MALE','FEMALE')),
    date_of_birth    DATE,
    color_markings   TEXT,
    weight_kg        DECIMAL(6,2),
    status           VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
                       CHECK (status IN ('ACTIVE','SOLD','DECEASED','QUARANTINED')),
    acquisition_type VARCHAR(20) CHECK (acquisition_type IN ('BORN_ON_FARM','PURCHASED')),
    mother_id        UUID REFERENCES animals(id),
    father_id        UUID REFERENCES animals(id),
    notes            TEXT,
    photo_url        VARCHAR(500),
    created_at       TIMESTAMPTZ DEFAULT NOW(),
    updated_at       TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT uq_tag_per_farm UNIQUE (farm_id, tag_number)
);

-- Indexes
CREATE INDEX idx_animals_farm_id       ON animals(farm_id);
CREATE INDEX idx_animals_herd_id       ON animals(herd_id);
CREATE INDEX idx_animals_status        ON animals(status);
CREATE INDEX idx_animals_type_gender   ON animals(animal_type, gender);
CREATE INDEX idx_animals_dob           ON animals(date_of_birth);
```

---

## 3. Health Service Schema (`farm_health`)

```sql
CREATE TABLE health_records (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    animal_id      UUID NOT NULL,           -- from Animal Service (no FK, different DB)
    farm_id        UUID NOT NULL,
    record_type    VARCHAR(30) NOT NULL
                     CHECK (record_type IN ('VACCINATION','TREATMENT','VET_VISIT','OBSERVATION')),
    record_date    DATE NOT NULL,
    description    VARCHAR(500),
    diagnosed_by   VARCHAR(255),
    notes          TEXT,
    next_due_date  DATE,
    created_by     UUID NOT NULL,
    created_at     TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE vaccinations (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    health_record_id  UUID NOT NULL REFERENCES health_records(id),
    vaccine_name      VARCHAR(255) NOT NULL,
    batch_number      VARCHAR(100),
    dosage_ml         DECIMAL(6,2),
    administered_by   VARCHAR(255),
    next_dose_date    DATE,
    manufacturer      VARCHAR(255)
);

CREATE TABLE treatments (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    health_record_id UUID NOT NULL REFERENCES health_records(id),
    medicine_name    VARCHAR(255) NOT NULL,
    dosage           VARCHAR(100),
    frequency        VARCHAR(100),
    start_date       DATE,
    end_date         DATE,
    cost             DECIMAL(10,2),
    prescribed_by    VARCHAR(255)
);

CREATE TABLE health_attachments (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    health_record_id UUID NOT NULL REFERENCES health_records(id),
    file_url         VARCHAR(500) NOT NULL,
    file_name        VARCHAR(255),
    uploaded_at      TIMESTAMPTZ DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_health_animal_id    ON health_records(animal_id);
CREATE INDEX idx_health_record_type  ON health_records(record_type);
CREATE INDEX idx_health_next_due     ON health_records(next_due_date) WHERE next_due_date IS NOT NULL;
```

---

## 4. Breeding Service Schema (`farm_breeding`)

```sql
CREATE TABLE mating_events (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    farm_id           UUID NOT NULL,
    female_animal_id  UUID NOT NULL,
    male_animal_id    UUID,               -- nullable if AI
    external_sire_id  VARCHAR(100),       -- AI straw ID
    mating_date       DATE NOT NULL,
    mating_type       VARCHAR(30) CHECK (mating_type IN ('NATURAL','ARTIFICIAL_INSEMINATION')),
    notes             TEXT,
    recorded_by       UUID NOT NULL,
    created_at        TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE pregnancy_records (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mating_event_id     UUID NOT NULL REFERENCES mating_events(id),
    female_animal_id    UUID NOT NULL,
    animal_type         VARCHAR(10),      -- COW or GOAT (determines gestation days)
    expected_due_date   DATE NOT NULL,    -- auto-calc: COW +280d, GOAT +150d
    confirmed_date      DATE,
    confirmed_by        VARCHAR(255),
    status              VARCHAR(20) NOT NULL DEFAULT 'SUSPECTED'
                          CHECK (status IN ('SUSPECTED','CONFIRMED','ABORTED','DELIVERED')),
    notes               TEXT,
    created_at          TIMESTAMPTZ DEFAULT NOW(),
    updated_at          TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE birth_records (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pregnancy_id         UUID NOT NULL REFERENCES pregnancy_records(id),
    female_animal_id     UUID NOT NULL,
    birth_date           DATE NOT NULL,
    number_of_offspring  INT NOT NULL,
    complications        TEXT,
    attended_by          VARCHAR(255),
    notes                TEXT,
    created_at           TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE offspring_registrations (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    birth_record_id UUID NOT NULL REFERENCES birth_records(id),
    offspring_id    UUID NOT NULL,       -- ID in Animal Service after registration
    gender          VARCHAR(10),
    birth_weight_kg DECIMAL(6,2)
);

CREATE INDEX idx_mating_female ON mating_events(female_animal_id);
CREATE INDEX idx_pregnancy_due ON pregnancy_records(expected_due_date);
CREATE INDEX idx_pregnancy_status ON pregnancy_records(status);
```

---

## 5. Milk Service Schema (TimescaleDB `farm_milk`)

```sql
-- Hypertable partitioned by record_date
CREATE TABLE milk_production (
    id              UUID NOT NULL DEFAULT gen_random_uuid(),
    animal_id       UUID NOT NULL,
    farm_id         UUID NOT NULL,
    record_date     DATE NOT NULL,
    session         VARCHAR(15) NOT NULL CHECK (session IN ('MORNING','AFTERNOON','EVENING')),
    quantity_liters DECIMAL(6,2) NOT NULL,
    fat_percentage  DECIMAL(4,2),
    snf_percentage  DECIMAL(4,2),
    recorded_by     UUID,
    recorded_at     TIMESTAMPTZ DEFAULT NOW(),
    PRIMARY KEY (id, record_date)           -- composite PK required for hypertable
);

-- Convert to TimescaleDB hypertable (partitioned by month)
SELECT create_hypertable('milk_production', 'record_date', chunk_time_interval => INTERVAL '1 month');

CREATE TABLE lactation_cycles (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    animal_id         UUID NOT NULL,
    farm_id           UUID NOT NULL,
    birth_record_id   UUID,               -- from Breeding Service
    lactation_number  INT NOT NULL,
    start_date        DATE NOT NULL,
    end_date          DATE,               -- null if still in lactation
    total_yield_liters DECIMAL(10,2),     -- computed on close
    peak_yield_liters  DECIMAL(6,2),
    created_at        TIMESTAMPTZ DEFAULT NOW()
);

-- Continuous aggregate for daily totals per animal (TimescaleDB feature)
CREATE MATERIALIZED VIEW daily_milk_totals
WITH (timescaledb.continuous) AS
SELECT
    animal_id,
    farm_id,
    time_bucket('1 day', record_date::TIMESTAMPTZ) AS bucket_date,
    SUM(quantity_liters) AS total_liters,
    COUNT(*) AS sessions_count
FROM milk_production
GROUP BY animal_id, farm_id, bucket_date;

CREATE INDEX idx_milk_animal_date ON milk_production(animal_id, record_date DESC);
```

---

## 6. Inventory Service Schema (`farm_inventory`)

```sql
CREATE TABLE feed_items (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    farm_id         UUID NOT NULL,
    name            VARCHAR(255) NOT NULL,
    category        VARCHAR(30) CHECK (category IN ('ROUGHAGE','CONCENTRATE','SUPPLEMENT','MINERAL')),
    unit            VARCHAR(20) NOT NULL,
    current_stock   DECIMAL(10,2) NOT NULL DEFAULT 0,
    minimum_stock   DECIMAL(10,2) NOT NULL DEFAULT 0,
    cost_per_unit   DECIMAL(10,2),
    created_at      TIMESTAMPTZ DEFAULT NOW(),
    updated_at      TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE feed_consumption_logs (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    farm_id          UUID NOT NULL,
    feed_item_id     UUID NOT NULL REFERENCES feed_items(id),
    animal_id        UUID,               -- nullable (herd-level logging)
    herd_id          UUID,               -- nullable (individual logging)
    consumption_date DATE NOT NULL,
    quantity_used    DECIMAL(10,2) NOT NULL,
    recorded_by      UUID NOT NULL,
    created_at       TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE feed_procurements (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    farm_id             UUID NOT NULL,
    feed_item_id        UUID NOT NULL REFERENCES feed_items(id),
    purchase_date       DATE NOT NULL,
    quantity_purchased  DECIMAL(10,2) NOT NULL,
    total_cost          DECIMAL(12,2) NOT NULL,
    supplier_name       VARCHAR(255),
    invoice_number      VARCHAR(100),
    receipt_url         VARCHAR(500),
    created_by          UUID NOT NULL,
    created_at          TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_feed_item_farm ON feed_items(farm_id);
CREATE INDEX idx_consumption_date ON feed_consumption_logs(consumption_date);
```

---

## 7. Finance Service Schema (`farm_finance`)

```sql
CREATE TABLE financial_transactions (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    farm_id          UUID NOT NULL,
    transaction_type VARCHAR(20) NOT NULL CHECK (transaction_type IN ('INCOME','EXPENSE')),
    category         VARCHAR(50) NOT NULL,
    -- Categories: ANIMAL_PURCHASE, ANIMAL_SALE, MILK_SALE, FEED_PURCHASE,
    --             VET_FEE, MEDICINE, EQUIPMENT, LABOR, UTILITIES, OTHER
    animal_id        UUID,               -- nullable, for animal-specific transactions
    amount           DECIMAL(12,2) NOT NULL,
    transaction_date DATE NOT NULL,
    description      TEXT,
    receipt_url      VARCHAR(500),
    is_reversal      BOOLEAN DEFAULT FALSE,
    reversed_txn_id  UUID REFERENCES financial_transactions(id),
    recorded_by      UUID NOT NULL,
    created_at       TIMESTAMPTZ DEFAULT NOW()
    -- No updates allowed — use reversal pattern
);

CREATE INDEX idx_finance_farm_date     ON financial_transactions(farm_id, transaction_date DESC);
CREATE INDEX idx_finance_category      ON financial_transactions(category);
CREATE INDEX idx_finance_animal        ON financial_transactions(animal_id) WHERE animal_id IS NOT NULL;
CREATE INDEX idx_finance_type_date     ON financial_transactions(transaction_type, transaction_date);
```

---

## 8. Indexing Strategy Summary

| Principle | Implementation |
|---|---|
| All FKs are indexed | Done across all schemas |
| Date range queries | Descending date indexes on all date columns used for reporting |
| Status filters | Partial indexes where status filters are selective |
| Composite indexes | (farm_id + date) for all tenant-scoped queries |
| TimescaleDB chunks | Monthly chunks for milk data — efficient range scans |
| Redis cache | Hot data (animal profiles, daily totals) cached for 30 min |

---

## 9. Multi-Tenancy

- All tables include `farm_id` — this is the **tenant identifier**
- Row-Level Security (RLS) enabled on PostgreSQL:
  ```sql
  ALTER TABLE animals ENABLE ROW LEVEL SECURITY;
  CREATE POLICY farm_isolation ON animals
      USING (farm_id = current_setting('app.current_farm_id')::UUID);
  ```
- `farm_id` injected from JWT claims via Spring Boot's `TenantContext`
