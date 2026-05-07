-- Migracion V3__Create_dossier_tables.sql
-- Crear tablas para el modulo Dossier (Digital Kardex, Assets, Talent Inventory)

CREATE TABLE IF NOT EXISTS document_record (
    doc_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    relationship_id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    doc_category VARCHAR(20) NOT NULL,
    doc_type VARCHAR(50) NOT NULL,
    is_critical BOOLEAN NOT NULL,

    current_state VARCHAR(20) NOT NULL,
    reviewer_id UUID,
    review_date TIMESTAMP,
    reject_reason TEXT,

    storage_id UUID NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    hash_sha256 VARCHAR(64) NOT NULL,
    expiry_date DATE
);

CREATE INDEX idx_document_record_relationship ON document_record(relationship_id);
CREATE INDEX idx_document_record_tenant ON document_record(tenant_id);
CREATE INDEX idx_document_record_category ON document_record(doc_category);

CREATE TABLE IF NOT EXISTS assigned_asset (
    assignment_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    worker_id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    asset_tag VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    returned_at TIMESTAMP,

    category VARCHAR(30) NOT NULL,
    tech_specs JSONB,
    initial_state TEXT
);

CREATE INDEX idx_assigned_asset_worker ON assigned_asset(worker_id);
CREATE INDEX idx_assigned_asset_tenant ON assigned_asset(tenant_id);
CREATE INDEX idx_assigned_asset_status ON assigned_asset(status);

CREATE TABLE IF NOT EXISTS talent_inventory (
    inventory_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    relationship_id UUID NOT NULL,
    tenant_id UUID NOT NULL
);

CREATE INDEX idx_talent_inventory_relationship ON talent_inventory(relationship_id);
CREATE INDEX idx_talent_inventory_tenant ON talent_inventory(tenant_id);

CREATE TABLE IF NOT EXISTS performance_snapshot (
    snapshot_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    inventory_id UUID NOT NULL,
    eval_period VARCHAR(20) NOT NULL,
    score DECIMAL(5,2) NOT NULL,

    FOREIGN KEY (inventory_id) REFERENCES talent_inventory(inventory_id) ON DELETE CASCADE
);

CREATE INDEX idx_performance_snapshot_inventory ON performance_snapshot(inventory_id);

CREATE TABLE IF NOT EXISTS skill_set (
    skill_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    inventory_id UUID NOT NULL,
    skill_name VARCHAR(100) NOT NULL,
    proficiency VARCHAR(20) NOT NULL,

    FOREIGN KEY (inventory_id) REFERENCES talent_inventory(inventory_id) ON DELETE CASCADE
);

CREATE INDEX idx_skill_set_inventory ON skill_set(inventory_id);

CREATE TABLE IF NOT EXISTS training_history (
    training_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    inventory_id UUID NOT NULL,
    course_name VARCHAR(200) NOT NULL,
    doc_id UUID,

    FOREIGN KEY (inventory_id) REFERENCES talent_inventory(inventory_id) ON DELETE CASCADE
);

CREATE INDEX idx_training_history_inventory ON training_history(inventory_id);

