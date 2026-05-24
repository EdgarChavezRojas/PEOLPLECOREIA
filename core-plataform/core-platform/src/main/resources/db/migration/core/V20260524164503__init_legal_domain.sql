-- Flyway Migration: V20260524164503__init_legal_domain
-- Description: Initialize Legal tables (Contract, Addendum, Policy, Threshold)

-- 1. legal_contract Table
CREATE TABLE legal_contract (
  contract_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  relationship_id UUID NOT NULL,
  contract_type VARCHAR(20) NOT NULL,
  status VARCHAR(20) NOT NULL,
  project_id UUID,
  employment_cond VARCHAR(2),
  tacita_reconduccion_alert_sent BOOLEAN NOT NULL,
  CONSTRAINT fk_legal_contract_relationship FOREIGN KEY (relationship_id) REFERENCES relationship (relationship_id)
);
CREATE INDEX idx_legal_contract_contract_id ON legal_contract (contract_id);
CREATE INDEX idx_legal_contract_tenant_id ON legal_contract (tenant_id);
CREATE INDEX idx_legal_contract_status ON legal_contract (status);

-- 2. legal_contract_addendum Table
CREATE TABLE legal_contract_addendum (
  addendum_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  contract_id UUID NOT NULL,
  status VARCHAR(20) NOT NULL,
  effective_from DATE NOT NULL,
  effective_to DATE NOT NULL,
  basic_salary NUMERIC(15, 2),
  total_earned_proj NUMERIC(15, 2),
  net_salary_proj NUMERIC(15, 2),
  currency VARCHAR(3),
  smn_applied NUMERIC(15, 2),
  tax_regime VARCHAR(50),
  infocal_active BOOLEAN,
  CONSTRAINT fk_legal_addendum_contract FOREIGN KEY (contract_id) REFERENCES legal_contract (contract_id)
);
CREATE INDEX idx_legal_addendum_addendum_id ON legal_contract_addendum (addendum_id);
CREATE INDEX idx_legal_addendum_contract_id ON legal_contract_addendum (contract_id);
CREATE INDEX idx_legal_addendum_tenant_id ON legal_contract_addendum (tenant_id);

-- 3. legal_policy_rule Table
CREATE TABLE legal_policy_rule (
  policy_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  policy_name VARCHAR(100) NOT NULL,
  description TEXT
);
CREATE INDEX idx_legal_policy_rule_policy_id ON legal_policy_rule (policy_id);
CREATE INDEX idx_legal_policy_rule_tenant_id ON legal_policy_rule (tenant_id);

-- 4. legal_policy_threshold Table (Element Collection)
CREATE TABLE legal_policy_threshold (
  policy_id UUID NOT NULL,
  threshold_value NUMERIC(15, 4),
  effective_date DATE,
  CONSTRAINT fk_legal_threshold_policy FOREIGN KEY (policy_id) REFERENCES legal_policy_rule (policy_id)
);
