-- Flyway Migration: V20260524164504__init_dossier_domain
-- Description: Initialize Dossier tables (AssignedAsset, DocumentRecord, TalentInventory, PerformanceSnapshot, SkillSet, TrainingHistory)

-- 1. assigned_asset Table
CREATE TABLE assigned_asset (
  assignment_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  worker_id UUID NOT NULL,
  asset_tag VARCHAR(255) NOT NULL,
  status VARCHAR(255) NOT NULL,
  assigned_at TIMESTAMP NOT NULL,
  returned_at TIMESTAMP,
  category VARCHAR(255) NOT NULL,
  tech_specs JSONB,
  initial_state VARCHAR(255)
);
CREATE INDEX idx_assigned_asset_worker ON assigned_asset (worker_id);
CREATE INDEX idx_assigned_asset_tenant ON assigned_asset (tenant_id);
CREATE INDEX idx_assigned_asset_status ON assigned_asset (status);

-- 2. document_record Table
CREATE TABLE document_record (
  doc_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  relationship_id UUID NOT NULL,
  doc_category VARCHAR(255) NOT NULL,
  doc_type VARCHAR(255) NOT NULL,
  is_critical BOOLEAN NOT NULL,
  current_state VARCHAR(255) NOT NULL,
  reviewer_id UUID,
  review_date TIMESTAMP,
  reject_reason VARCHAR(255),
  storage_id UUID NOT NULL,
  file_name VARCHAR(255) NOT NULL,
  hash_sha256 VARCHAR(255) NOT NULL,
  expiry_date DATE,
  expiration_warning_sent BOOLEAN NOT NULL,
  CONSTRAINT fk_document_record_relationship FOREIGN KEY (relationship_id) REFERENCES relationship (relationship_id)
);
CREATE INDEX idx_document_record_relationship ON document_record (relationship_id);
CREATE INDEX idx_document_record_tenant ON document_record (tenant_id);
CREATE INDEX idx_document_record_category ON document_record (doc_category);

-- 3. talent_inventory Table
CREATE TABLE talent_inventory (
  inventory_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  relationship_id UUID NOT NULL,
  CONSTRAINT fk_talent_inventory_relationship FOREIGN KEY (relationship_id) REFERENCES relationship (relationship_id)
);
CREATE INDEX idx_talent_inventory_relationship ON talent_inventory (relationship_id);
CREATE INDEX idx_talent_inventory_tenant ON talent_inventory (tenant_id);

-- 4. performance_snapshot Table
CREATE TABLE performance_snapshot (
  snapshot_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  eval_period VARCHAR(255) NOT NULL,
  score NUMERIC NOT NULL,
  inventory_id UUID NOT NULL,
  CONSTRAINT fk_performance_snapshot_inventory FOREIGN KEY (inventory_id) REFERENCES talent_inventory (inventory_id)
);

-- 5. skill_set Table
CREATE TABLE skill_set (
  skill_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  skill_name VARCHAR(255) NOT NULL,
  proficiency VARCHAR(255) NOT NULL,
  inventory_id UUID NOT NULL,
  CONSTRAINT fk_skill_set_inventory FOREIGN KEY (inventory_id) REFERENCES talent_inventory (inventory_id)
);

-- 6. training_history Table
CREATE TABLE training_history (
  training_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  course_name VARCHAR(255) NOT NULL,
  doc_id UUID,
  inventory_id UUID NOT NULL,
  CONSTRAINT fk_training_history_doc FOREIGN KEY (doc_id) REFERENCES document_record (doc_id),
  CONSTRAINT fk_training_history_inventory FOREIGN KEY (inventory_id) REFERENCES talent_inventory (inventory_id)
);
