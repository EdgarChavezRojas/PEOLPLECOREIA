ALTER TABLE attendance_ledger
    ADD COLUMN org_unit_id UUID;


ALTER TABLE attendance_ledger
    ALTER COLUMN org_unit_id SET NOT NULL;

