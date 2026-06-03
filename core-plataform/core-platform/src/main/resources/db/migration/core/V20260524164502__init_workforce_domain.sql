-- Flyway Migration: V20260524164502__init_workforce_domain
-- Description: Initialize Workforce tables (Person, Job, OrgUnit, Relationship, Position, etc.)

-- 1. person Table
CREATE TABLE person (
  person_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  first_name VARCHAR(255) NOT NULL,
  last_name VARCHAR(255) NOT NULL,
  birth_date DATE NOT NULL,
  gender VARCHAR(255),
  marital_status VARCHAR(12),
  profession_title VARCHAR(255),
  DNI VARCHAR(255),
  user_id BIGINT UNIQUE,
  global_id VARCHAR(255) NOT NULL UNIQUE,
  email VARCHAR(255),
  phone VARCHAR(255),
  address VARCHAR(255)
);
CREATE UNIQUE INDEX idx_person_global_id ON person (global_id);

-- 2. party_identifier Table
CREATE TABLE party_identifier (
  identifier_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  person_id UUID NOT NULL,
  id_type VARCHAR(255) NOT NULL,
  id_number VARCHAR(255) NOT NULL UNIQUE,
  extension VARCHAR(255),
  issue_date DATE,
  expiry_date DATE,
  CONSTRAINT fk_party_identifier_person FOREIGN KEY (person_id) REFERENCES person (person_id)
);
CREATE INDEX idx_identifier_person_id ON party_identifier (person_id);
CREATE UNIQUE INDEX idx_identifier_id_number ON party_identifier (id_number);
CREATE INDEX idx_identifier_expiry ON party_identifier (expiry_date);

-- 3. relationship Table
CREATE TABLE relationship (
  relationship_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  person_id UUID NOT NULL,
  rel_type VARCHAR(255) NOT NULL,
  current_status VARCHAR(255) NOT NULL,
  hire_date DATE,
  CONSTRAINT fk_relationship_person FOREIGN KEY (person_id) REFERENCES person (person_id)
);
CREATE INDEX idx_relationship_person_id ON relationship (person_id);
CREATE INDEX idx_relationship_tenant_id ON relationship (tenant_id);
CREATE INDEX idx_relationship_status ON relationship (current_status);

-- 4. worker_profile Table
CREATE TABLE worker_profile (
  profile_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  relationship_id UUID NOT NULL UNIQUE,
  employee_no VARCHAR(255) NOT NULL,
  department VARCHAR(255),
  job_title VARCHAR(255),
  CONSTRAINT fk_worker_profile_relationship FOREIGN KEY (relationship_id) REFERENCES relationship (relationship_id)
);
CREATE UNIQUE INDEX idx_worker_profile_relationship_id ON worker_profile (relationship_id);
CREATE INDEX idx_worker_profile_employee_no ON worker_profile (employee_no);

-- 5. academic_profile Table
CREATE TABLE academic_profile (
  academic_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  relationship_id UUID NOT NULL UNIQUE,
  current_rank VARCHAR(255),
  teaching_load INTEGER,
  CONSTRAINT fk_academic_profile_relationship FOREIGN KEY (relationship_id) REFERENCES relationship (relationship_id)
);
CREATE UNIQUE INDEX idx_academic_profile_relationship_id ON academic_profile (relationship_id);
CREATE INDEX idx_academic_profile_rank ON academic_profile (current_rank);

-- 6. status_log Table
CREATE TABLE status_log (
  log_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  relationship_id UUID NOT NULL,
  previous_status VARCHAR(255),
  new_status VARCHAR(255) NOT NULL,
  change_reason VARCHAR(255),
  changed_at DATE NOT NULL,
  changed_by UUID,
  CONSTRAINT fk_status_log_relationship FOREIGN KEY (relationship_id) REFERENCES relationship (relationship_id)
);
CREATE INDEX idx_status_log_relationship_id ON status_log (relationship_id);
CREATE INDEX idx_status_log_changed_at ON status_log (changed_at);

-- 7. job Table
CREATE TABLE job (
  job_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  job_code VARCHAR(255) NOT NULL UNIQUE,
  title VARCHAR(255) NOT NULL,
  grade_band VARCHAR(255),
  description VARCHAR(255)
);
CREATE UNIQUE INDEX idx_job_code ON job (job_code);

-- 8. org_unit Table
CREATE TABLE org_unit (
  unit_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  parent_id UUID,
  name VARCHAR(255) NOT NULL,
  unit_type VARCHAR(255),
  cost_code VARCHAR(255),
  description VARCHAR(255),
  geo_coords VARCHAR(255),
  is_root BOOLEAN NOT NULL
);
CREATE INDEX idx_org_unit_tenant_id ON org_unit (tenant_id);
CREATE INDEX idx_org_unit_parent_id ON org_unit (parent_id);

-- 9. org_hierarchy Table
CREATE TABLE org_hierarchy (
  hierarchy_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  parent_unit_id UUID NOT NULL,
  child_unit_id UUID NOT NULL,
  hierarchy_type VARCHAR(255),
  effective_date DATE NOT NULL,
  end_date DATE,
  CONSTRAINT fk_org_hierarchy_child FOREIGN KEY (child_unit_id) REFERENCES org_unit (unit_id)
);
CREATE INDEX idx_hierarchy_parent ON org_hierarchy (parent_unit_id);
CREATE INDEX idx_hierarchy_child ON org_hierarchy (child_unit_id);
CREATE INDEX idx_hierarchy_effective ON org_hierarchy (effective_date);

-- 10. position Table
CREATE TABLE position (
  position_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  unit_id UUID NOT NULL,
  job_id UUID NOT NULL,
  pos_status VARCHAR(255) NOT NULL,
  is_budgeted BOOLEAN,
  max_slots INTEGER,
  current_slots INTEGER,
  CONSTRAINT fk_position_unit FOREIGN KEY (unit_id) REFERENCES org_unit (unit_id),
  CONSTRAINT fk_position_job FOREIGN KEY (job_id) REFERENCES job (job_id)
);
CREATE INDEX idx_position_unit_id ON position (unit_id);
CREATE INDEX idx_position_status ON position (pos_status);

CREATE TABLE tenant (
                        tenant_id UUID NOT NULL PRIMARY KEY,
                        name VARCHAR(255) NOT NULL,
                        status VARCHAR(50) NOT NULL,
                        description TEXT,
                        created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                        updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Índice único en name (coincide con unique = true y nombre de índice solicitado)
CREATE UNIQUE INDEX idx_tenant_name ON tenant (name);
