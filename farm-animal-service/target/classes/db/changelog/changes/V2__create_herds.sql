--liquibase formatted sql

--changeset author:herd_create_table
CREATE TABLE herds (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    farm_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    herd_type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

--changeset author:herd_add_indexes
CREATE INDEX idx_herds_farm_id ON herds(farm_id);
CREATE INDEX idx_herds_name ON herds(name);

--changeset author:herd_add_constraints
ALTER TABLE herds ADD CONSTRAINT chk_herd_type CHECK (herd_type IN ('MILKING', 'BREEDING', 'Calf', 'OTHER'));
ALTER TABLE herds ADD CONSTRAINT uk_herd_name_farm UNIQUE (name, farm_id);