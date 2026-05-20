CREATE TABLE training_history (
  training_id UUID PRIMARY KEY,
  course_name VARCHAR(255) NOT NULL,
  doc_id UUID,
  inventory_id UUID NOT NULL,
  CONSTRAINT fk_training_history_inventory
    FOREIGN KEY (inventory_id) REFERENCES talent_inventory (inventory_id)
);

