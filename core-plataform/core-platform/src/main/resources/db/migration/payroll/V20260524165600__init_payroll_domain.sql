-- Flyway Migration: V20260524165600__init_payroll_domain
-- Description: Initialize Payroll tables (BankEntity, DispersionFile, DeductionRecord, IncomeRecord, PaymentMethod, PayrollApproval, Closure, Group, Line, Period, Run)

-- 1. prl_bank_entity Table
CREATE TABLE prl_bank_entity (
  bank_entity_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  bank_code VARCHAR(30) NOT NULL,
  name VARCHAR(150) NOT NULL,
  file_format VARCHAR(100)
);

-- 2. prl_payroll_group Table
CREATE TABLE prl_payroll_group (
  group_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  group_code VARCHAR(50) NOT NULL,
  type_code VARCHAR(30) NOT NULL,
  description VARCHAR(255)
);

-- 3. prl_payment_method Table
CREATE TABLE prl_payment_method (
  payment_method_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  channel VARCHAR(30) NOT NULL,
  is_default BOOLEAN NOT NULL
);

-- 4. prl_payroll_period Table
CREATE TABLE prl_payroll_period (
  period_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  month INTEGER NOT NULL,
  year INTEGER NOT NULL,
  cutoff_date DATE NOT NULL,
  status VARCHAR(20) NOT NULL,
  holiday_calendar_ref UUID,
  CONSTRAINT fk_prl_period_holiday FOREIGN KEY (holiday_calendar_ref) REFERENCES holiday_calendar (holiday_id)
);

-- 5. prl_deduction_record Table
CREATE TABLE prl_deduction_record (
  deduction_record_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  employee_id UUID NOT NULL,
  period_ref UUID NOT NULL,
  deduction_type VARCHAR(50) NOT NULL,
  amount NUMERIC(19, 4) NOT NULL,
  is_automatic BOOLEAN NOT NULL,
  CONSTRAINT fk_prl_deduction_period FOREIGN KEY (period_ref) REFERENCES prl_payroll_period (period_id),
  CONSTRAINT fk_prl_deduction_employee FOREIGN KEY (employee_id) REFERENCES relationship (relationship_id)
);

-- 6. prl_income_record Table
CREATE TABLE prl_income_record (
  income_record_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  employee_id UUID NOT NULL,
  period_ref UUID NOT NULL,
  income_type VARCHAR(50) NOT NULL,
  amount NUMERIC(19, 4) NOT NULL,
  is_automatic BOOLEAN NOT NULL,
  CONSTRAINT fk_prl_income_period FOREIGN KEY (period_ref) REFERENCES prl_payroll_period (period_id),
  CONSTRAINT fk_prl_income_employee FOREIGN KEY (employee_id) REFERENCES relationship (relationship_id)
);

-- 7. prl_payroll_run Table
CREATE TABLE prl_payroll_run (
  payroll_run_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  period_ref UUID,
  group_ref UUID,
  run_type VARCHAR(255),
  status VARCHAR(255),
  CONSTRAINT fk_prl_run_period FOREIGN KEY (period_ref) REFERENCES prl_payroll_period (period_id),
  CONSTRAINT fk_prl_run_group FOREIGN KEY (group_ref) REFERENCES prl_payroll_group (group_id)
);

-- 8. prl_payroll_line Table
CREATE TABLE prl_payroll_line (
  line_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  run_id UUID,
  employee_id UUID,
  basic_salary NUMERIC,
  total_earned NUMERIC,
  rc_iva_retained NUMERIC,
  gestora_retained NUMERIC,
  other_deductions NUMERIC,
  net_payable NUMERIC,
  seniority_bonus NUMERIC,
  infocal_retained NUMERIC,
  fiscal_credit NUMERIC,
  CONSTRAINT fk_prl_line_run FOREIGN KEY (run_id) REFERENCES prl_payroll_run (payroll_run_id),
  CONSTRAINT fk_prl_line_employee FOREIGN KEY (employee_id) REFERENCES relationship (relationship_id)
);

-- 9. prl_payroll_closure Table
CREATE TABLE prl_payroll_closure (
  payroll_closure_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  run_ref UUID NOT NULL,
  status VARCHAR(255) NOT NULL,
  integrity_hash VARCHAR(255),
  CONSTRAINT fk_prl_closure_run FOREIGN KEY (run_ref) REFERENCES prl_payroll_run (payroll_run_id)
);

-- 10. prl_payroll_approval Table
CREATE TABLE prl_payroll_approval (
  payroll_approval_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  run_ref UUID NOT NULL,
  status VARCHAR(255) NOT NULL,
  creator_ref UUID NOT NULL,
  reviewer_ref UUID,
  approver_ref UUID,
  sod_violation_flag BOOLEAN NOT NULL,
  CONSTRAINT fk_prl_approval_run FOREIGN KEY (run_ref) REFERENCES prl_payroll_run (payroll_run_id)
);

-- 11. prl_dispersion_file Table
CREATE TABLE prl_dispersion_file (
  dispersion_file_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  run_ref UUID,
  bank_entity_ref UUID,
  status VARCHAR(255),
  total_amount NUMERIC,
  record_count INTEGER,
  file_hash VARCHAR(255),
  CONSTRAINT fk_prl_dispersion_run FOREIGN KEY (run_ref) REFERENCES prl_payroll_run (payroll_run_id),
  CONSTRAINT fk_prl_dispersion_bank FOREIGN KEY (bank_entity_ref) REFERENCES prl_bank_entity (bank_entity_id)
);
