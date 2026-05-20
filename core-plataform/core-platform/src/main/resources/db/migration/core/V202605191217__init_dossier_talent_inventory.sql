CREATE TABLE talent_inventory (
  inventory_id UUID PRIMARY KEY,
  relationship_id UUID NOT NULL,
  tenant_id UUID NOT NULL
);

CREATE INDEX idx_talent_inventory_relationship ON talent_inventory (relationship_id);
CREATE INDEX idx_talent_inventory_tenant ON talent_inventory (tenant_id);

