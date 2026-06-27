-- changes/V3__fix_herd_type_constraint.sql
ALTER TABLE herds DROP CONSTRAINT chk_herd_type;

ALTER TABLE herds ADD CONSTRAINT chk_herd_type
    CHECK (herd_type IN ('MILKING', 'BREEDING', 'CALF', 'OTHER'));