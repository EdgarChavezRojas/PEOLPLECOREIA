-- Flyway Migration: V20260524165000__init_accruals_domain
-- Description: Initialize Accruals tables (AccrualBalance, BenefitAccrual, HolidayCalendar, LeaveTransaction, QuinquenioProvision, SeniorityMilestone)

-- 1. accrual_balance Table
CREATE TABLE accrual_balance (
  balance_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  relationship_id UUID NOT NULL,
  balance_type VARCHAR(255) NOT NULL,
  unit VARCHAR(255) NOT NULL,
  current_balance NUMERIC NOT NULL,
  initial_balance NUMERIC,
  days_accrued_ytd NUMERIC,
  days_taken_ytd NUMERIC,
  last_accrual_date DATE,
  CONSTRAINT fk_accrual_balance_relationship FOREIGN KEY (relationship_id) REFERENCES relationship (relationship_id)
);
CREATE INDEX idx_accrual_balance_relationship ON accrual_balance (relationship_id);
CREATE INDEX idx_accrual_balance_tenant ON accrual_balance (tenant_id);
CREATE INDEX idx_accrual_balance_type ON accrual_balance (balance_type);

-- 2. benefit_accrual Table
CREATE TABLE benefit_accrual (
  benefit_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  relationship_id UUID NOT NULL,
  benefit_type VARCHAR(255) NOT NULL,
  fiscal_year INTEGER NOT NULL,
  accrued_amount NUMERIC NOT NULL,
  CONSTRAINT fk_benefit_accrual_relationship FOREIGN KEY (relationship_id) REFERENCES relationship (relationship_id)
);
CREATE INDEX idx_benefit_accrual_type_year ON benefit_accrual (benefit_type, fiscal_year);
CREATE INDEX idx_benefit_accrual_tenant ON benefit_accrual (tenant_id);

-- 3. holiday_calendar Table
CREATE TABLE holiday_calendar (
  holiday_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  holiday_date DATE NOT NULL,
  scope VARCHAR(255) NOT NULL
);
CREATE INDEX idx_holiday_calendar_date ON holiday_calendar (holiday_date);
CREATE INDEX idx_holiday_calendar_tenant ON holiday_calendar (tenant_id);

-- 4. leave_transaction Table
CREATE TABLE leave_transaction (
  transaction_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  balance_id UUID NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  days_requested NUMERIC NOT NULL,
  status VARCHAR(255) NOT NULL,
  CONSTRAINT fk_leave_transaction_balance FOREIGN KEY (balance_id) REFERENCES accrual_balance (balance_id)
);

-- 5. quinquenio_provision Table
CREATE TABLE quinquenio_provision (
  provision_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  relationship_id UUID NOT NULL,
  total_accumulated NUMERIC NOT NULL,
  penalty_active BOOLEAN NOT NULL,
  CONSTRAINT fk_quinquenio_relationship FOREIGN KEY (relationship_id) REFERENCES relationship (relationship_id)
);
CREATE INDEX idx_quinquenio_relationship ON quinquenio_provision (relationship_id);
CREATE INDEX idx_quinquenio_tenant ON quinquenio_provision (tenant_id);

-- 6. seniority_milestone Table
CREATE TABLE seniority_milestone (
  milestone_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  balance_id UUID NOT NULL,
  months_completed INTEGER NOT NULL,
  base_smn_type VARCHAR(255) NOT NULL,
  CONSTRAINT fk_seniority_milestone_balance FOREIGN KEY (balance_id) REFERENCES accrual_balance (balance_id)
);
