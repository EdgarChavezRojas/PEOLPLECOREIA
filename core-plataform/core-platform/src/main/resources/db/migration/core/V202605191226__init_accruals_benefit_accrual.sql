CREATE TABLE benefit_accrual (
  benefit_id UUID PRIMARY KEY,
  relationship_id UUID NOT NULL,
  benefit_type VARCHAR(255) NOT NULL,
  fiscal_year INTEGER NOT NULL,
  accrued_amount NUMERIC NOT NULL,
  tenant_id UUID NOT NULL
);

CREATE INDEX idx_benefit_accrual_type_year ON benefit_accrual (benefit_type, fiscal_year);
CREATE INDEX idx_benefit_accrual_tenant ON benefit_accrual (tenant_id);

