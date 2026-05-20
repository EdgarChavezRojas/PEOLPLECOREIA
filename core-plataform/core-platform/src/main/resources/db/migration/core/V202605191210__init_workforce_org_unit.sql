CREATE TABLE org_unit (
  unit_id UUID PRIMARY KEY,
  tenant_id UUID NOT NULL,
  parent_id UUID,
  name VARCHAR(255) NOT NULL,
  unit_type VARCHAR(255),
  cost_center_cost_code VARCHAR(255),
  cost_center_description VARCHAR(255),
  geo_coords VARCHAR(255),
  is_root BOOLEAN NOT NULL,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP
);

CREATE INDEX idx_org_unit_tenant_id ON org_unit (tenant_id);
CREATE INDEX idx_org_unit_parent_id ON org_unit (parent_id);

