-- Flyway Migration: V20260524164506__init_financial_domain
-- Description: Initialize Financial tables (FundingSource, HealthProvider, LaborCostSplit, SocialSecurityAccount, TaxForm110)

-- 1. funding_source Table
CREATE TABLE funding_source (
  source_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  project_code VARCHAR(50) NOT NULL,
  total_budget NUMERIC(18, 2) NOT NULL,
  available_budget NUMERIC(18, 2) NOT NULL,
  burn_rate NUMERIC(5, 2),
  created_by_user VARCHAR(255)
);

-- 2. health_provider Table
CREATE TABLE health_provider (
  provider_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  registration_no VARCHAR(50) NOT NULL,
  status VARCHAR(20) NOT NULL,
  created_by_user VARCHAR(255)
);

-- 3. labor_cost_split Table
CREATE TABLE labor_cost_split (
  split_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  unit_id UUID NOT NULL,
  percentage NUMERIC(5, 2) NOT NULL,
  effective_date DATE NOT NULL,
  funding_source_id UUID NOT NULL,
  CONSTRAINT fk_labor_cost_split_unit FOREIGN KEY (unit_id) REFERENCES org_unit (unit_id),
  CONSTRAINT fk_labor_cost_split_funding FOREIGN KEY (funding_source_id) REFERENCES funding_source (source_id)
);

-- 4. social_security_account Table
CREATE TABLE social_security_account (
  ssa_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  person_id UUID NOT NULL,
  gestora_code VARCHAR(30) NOT NULL,
  contribution_rate NUMERIC(5, 4) NOT NULL,
  last_contribution DATE,
  created_by_user VARCHAR(255),
  CONSTRAINT fk_social_security_person FOREIGN KEY (person_id) REFERENCES person (person_id)
);

-- 5. tax_form_110 Table
CREATE TABLE tax_form_110 (
  form_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  person_id UUID NOT NULL,
  total_declared NUMERIC(15, 2) NOT NULL,
  verified_credit NUMERIC(15, 2) NOT NULL,
  doc_id UUID,
  period_year INTEGER NOT NULL,
  period_month INTEGER NOT NULL,
  created_by_user VARCHAR(255),
  CONSTRAINT fk_tax_form_person FOREIGN KEY (person_id) REFERENCES person (person_id),
  CONSTRAINT fk_tax_form_doc FOREIGN KEY (doc_id) REFERENCES document_record (doc_id)
);
