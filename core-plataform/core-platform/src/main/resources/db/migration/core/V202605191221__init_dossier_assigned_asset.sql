CREATE TABLE assigned_asset (
  assignment_id UUID PRIMARY KEY,
  worker_id UUID NOT NULL,
  asset_tag VARCHAR(255) NOT NULL,
  status VARCHAR(255) NOT NULL,
  assigned_at TIMESTAMP NOT NULL,
  returned_at TIMESTAMP,
  category VARCHAR(255) NOT NULL,
  tech_specs JSONB,
  initial_state VARCHAR(255),
  tenant_id UUID NOT NULL
);

CREATE INDEX idx_assigned_asset_worker ON assigned_asset (worker_id);
CREATE INDEX idx_assigned_asset_tenant ON assigned_asset (tenant_id);
CREATE INDEX idx_assigned_asset_status ON assigned_asset (status);

