--liquibase formatted sql

--changeset author:animal_create_table
CREATE TABLE animals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    farm_id UUID NOT NULL,
    tag_number VARCHAR(50) NOT NULL UNIQUE,
    animal_type VARCHAR(20) NOT NULL,
    gender VARCHAR(20),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    date_of_birth DATE,
    herd_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

--changeset author:animal_add_indexes
CREATE INDEX idx_animals_farm_id ON animals(farm_id);
CREATE INDEX idx_animals_tag_number ON animals(tag_number);
CREATE INDEX idx_animals_herd_id ON animals(herd_id);
CREATE INDEX idx_animals_status ON animals(status);

--changeset author:animal_add_constraints
ALTER TABLE animals ADD CONSTRAINT chk_animal_type CHECK (animal_type IN ('COW', 'GOAT'));
ALTER TABLE animals ADD CONSTRAINT chk_gender CHECK (gender IN ('MALE', 'FEMALE', 'UNKNOWN'));
ALTER TABLE animals ADD CONSTRAINT chk_status CHECK (status IN ('ACTIVE', 'SOLD', 'DECEASED', 'QUARANTINED'));