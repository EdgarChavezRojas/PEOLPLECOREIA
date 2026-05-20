CREATE TABLE org_hierarchy (
  hierarchy_id UUID PRIMARY KEY,
  parent_unit_id UUID NOT NULL,
  child_unit_id UUID NOT NULL,
  hierarchy_type VARCHAR(255),
  effective_date DATE NOT NULL,
  end_date DATE,
  created_at TIMESTAMP NOT NULL,
  CONSTRAINT fk_org_hierarchy_child_unit
    FOREIGN KEY (child_unit_id) REFERENCES org_unit (unit_id)
);

CREATE INDEX idx_hierarchy_parent ON org_hierarchy (parent_unit_id);
CREATE INDEX idx_hierarchy_child ON org_hierarchy (child_unit_id);
CREATE INDEX idx_hierarchy_effective ON org_hierarchy (effective_date);

