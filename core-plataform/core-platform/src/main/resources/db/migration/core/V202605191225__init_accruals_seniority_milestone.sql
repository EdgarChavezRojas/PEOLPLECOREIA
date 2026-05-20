CREATE TABLE seniority_milestone (
  milestone_id UUID PRIMARY KEY,
  balance_id UUID NOT NULL,
  months_completed INTEGER NOT NULL,
  base_smn_type VARCHAR(255) NOT NULL,
  CONSTRAINT fk_seniority_milestone_balance
    FOREIGN KEY (balance_id) REFERENCES accrual_balance (balance_id)
);

