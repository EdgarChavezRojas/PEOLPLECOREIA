ALTER TABLE position
    ADD COLUMN max_slots INTEGER,
    ADD COLUMN current_slots INTEGER;

UPDATE position
SET max_slots = headcount_plan_max_slots,
    current_slots = headcount_plan_current_slots
WHERE max_slots IS NULL
  AND current_slots IS NULL;