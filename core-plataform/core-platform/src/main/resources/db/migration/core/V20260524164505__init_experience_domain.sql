-- Flyway Migration: V20260524164505__init_experience_domain
-- Description: Initialize Experience tables (SelfServiceAction, ApprovalWorkflow, Notification, PredictionModel)

-- 1. experience_self_service_action Table
CREATE TABLE experience_self_service_action (
  action_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  person_id UUID NOT NULL,
  action_type VARCHAR(30) NOT NULL,
  payload TEXT,
  cert_type VARCHAR(50),
  cert_pdf_content TEXT,
  cert_sha256_hash VARCHAR(64),
  cert_qr_url VARCHAR(512),
  cert_generated_at TIMESTAMP,
  CONSTRAINT fk_experience_self_service_person FOREIGN KEY (person_id) REFERENCES person (person_id)
);

-- 2. experience_approval_workflow Table
CREATE TABLE experience_approval_workflow (
  workflow_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  action_id UUID NOT NULL UNIQUE,
  current_step INTEGER NOT NULL,
  status VARCHAR(20) NOT NULL,
  history TEXT,
  CONSTRAINT fk_experience_approval_action FOREIGN KEY (action_id) REFERENCES experience_self_service_action (action_id)
);

-- 3. experience_notification Table
CREATE TABLE experience_notification (
  notif_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  recipient_id UUID NOT NULL,
  channel VARCHAR(20) NOT NULL,
  subject VARCHAR(255) NOT NULL,
  body TEXT,
  sent_at TIMESTAMP NOT NULL,
  read_at TIMESTAMP,
  read_by BOOLEAN,
  acknowledged_at TIMESTAMP NOT NULL,
  acknowledged_by UUID NOT NULL
);

-- 4. experience_prediction_model Table
CREATE TABLE experience_prediction_model (
  model_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  model_type VARCHAR(30) NOT NULL,
  last_execution TIMESTAMP,
  alerts TEXT
);
