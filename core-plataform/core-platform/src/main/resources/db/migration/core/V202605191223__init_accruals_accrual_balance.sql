CREATE TABLE accrual_balance (
  balance_id UUID PRIMARY KEY,
  relationship_id UUID NOT NULL,
  balance_type VARCHAR(255) NOT NULL,
  unit VARCHAR(255) NOT NULL,
  current_balance NUMERIC NOT NULL,
  initial_balance NUMERIC,
  days_accrued_ytd NUMERIC,
  days_taken_ytd NUMERIC,
  last_accrual_date DATE,
  tenant_id UUID NOT NULL
);

CREATE INDEX idx_accrual_balance_relationship ON accrual_balance (relationship_id);
CREATE INDEX idx_accrual_balance_tenant ON accrual_balance (tenant_id);
CREATE INDEX idx_accrual_balance_type ON accrual_balance (balance_type);

