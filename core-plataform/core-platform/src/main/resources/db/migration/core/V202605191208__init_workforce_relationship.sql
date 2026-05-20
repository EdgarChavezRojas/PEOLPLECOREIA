CREATE TABLE relationship (
  relationship_id UUID PRIMARY KEY,
  person_id UUID NOT NULL,
  tenant_id UUID NOT NULL,
  rel_type VARCHAR(255) NOT NULL,
  current_status VARCHAR(255) NOT NULL,
  hire_date DATE,
  created_at DATE NOT NULL,
  updated_at DATE
);

CREATE INDEX idx_relationship_person_id ON relationship (person_id);
CREATE INDEX idx_relationship_tenant_id ON relationship (tenant_id);
CREATE INDEX idx_relationship_status ON relationship (current_status);

