CREATE TABLE skill_set (
  skill_id UUID PRIMARY KEY,
  skill_name VARCHAR(255) NOT NULL,
  proficiency VARCHAR(255) NOT NULL,
  inventory_id UUID NOT NULL,
  CONSTRAINT fk_skill_set_inventory
    FOREIGN KEY (inventory_id) REFERENCES talent_inventory (inventory_id)
);

