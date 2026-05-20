ALTER TABLE org_unit
    ADD COLUMN cost_code VARCHAR(255),
    ADD COLUMN description VARCHAR(255);

UPDATE org_unit
SET cost_code = cost_center_cost_code,
    description = cost_center_description
WHERE cost_code IS NULL
  AND description IS NULL;