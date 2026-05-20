CREATE TABLE quinquenio_provision (
  provision_id UUID PRIMARY KEY,
  relationship_id UUID NOT NULL,
  total_accumulated NUMERIC NOT NULL,
  penalty_active BOOLEAN NOT NULL,
  tenant_id UUID NOT NULL
);

CREATE INDEX idx_quinquenio_relationship ON quinquenio_provision (relationship_id);
CREATE INDEX idx_quinquenio_tenant ON quinquenio_provision (tenant_id);

