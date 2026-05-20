CREATE TABLE performance_snapshot (
  snapshot_id UUID PRIMARY KEY,
  eval_period VARCHAR(255) NOT NULL,
  score NUMERIC NOT NULL,
  inventory_id UUID NOT NULL,
  CONSTRAINT fk_performance_snapshot_inventory
    FOREIGN KEY (inventory_id) REFERENCES talent_inventory (inventory_id)
);

