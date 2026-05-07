-- Migración V1__Create_workforce_tables.sql
-- Crear tablas para el módulo Workforce & Org Master

-- Extensiones
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- TABLA: PERSON (Aggregate Root: PersonIdentity)
CREATE TABLE IF NOT EXISTS person (
    person_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    birth_date DATE NOT NULL,
    gender VARCHAR(20),
    global_id VARCHAR(50) NOT NULL UNIQUE,

    -- Embedded Value Object: ContactPoint
    email VARCHAR(150),
    phone VARCHAR(20),
    address TEXT,

    created_at DATE NOT NULL DEFAULT CURRENT_DATE,
    updated_at DATE NOT NULL DEFAULT CURRENT_DATE,

    CONSTRAINT chk_age CHECK (EXTRACT(YEAR FROM age(birth_date)) >= 18)
);

CREATE INDEX idx_person_global_id ON person(global_id);
CREATE INDEX idx_person_first_last_name ON person(first_name, last_name);

-- TABLA: PARTY_IDENTIFIER (Entity dentro de PersonIdentity)
CREATE TABLE IF NOT EXISTS party_identifier (
    identifier_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    person_id UUID NOT NULL,
    id_type VARCHAR(20) NOT NULL,  -- CI, PASSPORT
    id_number VARCHAR(30) NOT NULL UNIQUE,
    extension VARCHAR(10),          -- SCZ, LP, CB, etc.
    issue_date DATE,
    expiry_date DATE,

    created_at DATE NOT NULL DEFAULT CURRENT_DATE,

    FOREIGN KEY (person_id) REFERENCES person(person_id) ON DELETE CASCADE,
    CONSTRAINT chk_dates CHECK (issue_date <= expiry_date OR expiry_date IS NULL)
);

CREATE INDEX idx_identifier_person_id ON party_identifier(person_id);
CREATE INDEX idx_identifier_id_number ON party_identifier(id_number);
CREATE INDEX idx_identifier_expiry ON party_identifier(expiry_date);

-- TABLA: ORG_UNIT (Aggregate Root: OrgStructure)
CREATE TABLE IF NOT EXISTS org_unit (
    unit_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    parent_id UUID,  -- NULL si es raíz
    name VARCHAR(150) NOT NULL,
    unit_type VARCHAR(20) NOT NULL,  -- ADMINISTRATIVE, ACADEMIC, COMMERCIAL

    -- Embedded Value Object: CostCenter
    cost_code VARCHAR(50) NOT NULL,
    cost_description VARCHAR(255),

    geo_coords TEXT,
    is_root BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (parent_id) REFERENCES org_unit(unit_id) ON DELETE RESTRICT,
    CONSTRAINT chk_hierarchy CHECK (
        (is_root = TRUE AND parent_id IS NULL) OR
        (is_root = FALSE AND parent_id IS NOT NULL)
    )
);

CREATE INDEX idx_org_unit_tenant_id ON org_unit(tenant_id);
CREATE INDEX idx_org_unit_parent_id ON org_unit(parent_id);
CREATE INDEX idx_org_unit_cost_code ON org_unit(cost_code);

-- TABLA: ORG_HIERARCHY (Entity dentro de OrgStructure)
CREATE TABLE IF NOT EXISTS org_hierarchy (
    hierarchy_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    parent_unit_id UUID NOT NULL,
    child_unit_id UUID NOT NULL,
    hierarchy_type VARCHAR(50),
    effective_date DATE NOT NULL DEFAULT CURRENT_DATE,
    end_date DATE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (parent_unit_id) REFERENCES org_unit(unit_id) ON DELETE CASCADE,
    FOREIGN KEY (child_unit_id) REFERENCES org_unit(unit_id) ON DELETE CASCADE,
    CONSTRAINT chk_no_self_hierarchy CHECK (parent_unit_id != child_unit_id),
    UNIQUE(parent_unit_id, child_unit_id, hierarchy_type)
);

CREATE INDEX idx_hierarchy_parent ON org_hierarchy(parent_unit_id);
CREATE INDEX idx_hierarchy_child ON org_hierarchy(child_unit_id);
CREATE INDEX idx_hierarchy_effective ON org_hierarchy(effective_date);

-- TABLA: JOB (Entity/VO: Descriptor de Cargos)
CREATE TABLE IF NOT EXISTS job (
    job_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    job_code VARCHAR(20) NOT NULL UNIQUE,
    title VARCHAR(150) NOT NULL,
    grade_band VARCHAR(10),
    description TEXT,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_job_code ON job(job_code);

-- TABLA: POSITION (Aggregate Root: PositionPlaza)
CREATE TABLE IF NOT EXISTS position (
    position_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    unit_id UUID NOT NULL,
    job_id UUID NOT NULL,
    pos_status VARCHAR(20) NOT NULL DEFAULT 'VACANT',
    is_budgeted BOOLEAN DEFAULT FALSE,

    -- Embedded Value Object: HeadcountPlan
    max_slots INTEGER NOT NULL,
    current_slots INTEGER NOT NULL DEFAULT 0,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (unit_id) REFERENCES org_unit(unit_id) ON DELETE RESTRICT,
    FOREIGN KEY (job_id) REFERENCES job(job_id) ON DELETE RESTRICT,
    CONSTRAINT chk_max_slots CHECK (max_slots > 0),
    CONSTRAINT chk_current_slots CHECK (current_slots >= 0 AND current_slots <= max_slots)
);

CREATE INDEX idx_position_unit_id ON position(unit_id);
CREATE INDEX idx_position_job_id ON position(job_id);
CREATE INDEX idx_position_status ON position(pos_status);

-- TABLA: RELATIONSHIP (Aggregate Root: EmploymentRelationship)
CREATE TABLE IF NOT EXISTS relationship (
    relationship_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    person_id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    rel_type VARCHAR(20) NOT NULL,
    current_status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    hire_date DATE,

    created_at DATE NOT NULL DEFAULT CURRENT_DATE,
    updated_at DATE NOT NULL DEFAULT CURRENT_DATE,

    FOREIGN KEY (person_id) REFERENCES person(person_id) ON DELETE RESTRICT,
    UNIQUE(person_id, tenant_id, rel_type)
);

CREATE INDEX idx_relationship_person_id ON relationship(person_id);
CREATE INDEX idx_relationship_tenant_id ON relationship(tenant_id);
CREATE INDEX idx_relationship_status ON relationship(current_status);

-- TABLA: WORKER_PROFILE (Entity dentro de Relationship)
CREATE TABLE IF NOT EXISTS worker_profile (
    profile_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    relationship_id UUID NOT NULL UNIQUE,
    employee_no VARCHAR(20) NOT NULL,
    department VARCHAR(100),
    job_title VARCHAR(150),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (relationship_id) REFERENCES relationship(relationship_id) ON DELETE CASCADE
);

CREATE INDEX idx_worker_profile_relationship_id ON worker_profile(relationship_id);
CREATE INDEX idx_worker_profile_employee_no ON worker_profile(employee_no);

-- TABLA: ACADEMIC_PROFILE (Entity dentro de Relationship)
CREATE TABLE IF NOT EXISTS academic_profile (
    academic_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    relationship_id UUID NOT NULL UNIQUE,
    current_rank VARCHAR(30),
    teaching_load INTEGER,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (relationship_id) REFERENCES relationship(relationship_id) ON DELETE CASCADE,
    CONSTRAINT chk_teaching_load CHECK (teaching_load > 0)
);

CREATE INDEX idx_academic_profile_relationship_id ON academic_profile(relationship_id);
CREATE INDEX idx_academic_profile_rank ON academic_profile(current_rank);

-- TABLA: STATUS_LOG (Entity dentro de Relationship)
CREATE TABLE IF NOT EXISTS status_log (
    log_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    relationship_id UUID NOT NULL,
    previous_status VARCHAR(20),
    new_status VARCHAR(20) NOT NULL,
    change_reason TEXT,
    changed_at DATE NOT NULL DEFAULT CURRENT_DATE,
    changed_by UUID,

    FOREIGN KEY (relationship_id) REFERENCES relationship(relationship_id) ON DELETE CASCADE,
    CONSTRAINT chk_status_different CHECK (previous_status IS DISTINCT FROM new_status)
);

CREATE INDEX idx_status_log_relationship_id ON status_log(relationship_id);
CREATE INDEX idx_status_log_changed_at ON status_log(changed_at);

-- TABLA: EVENT_OUTBOX (Patrón Outbox para Consistencia Eventual)
CREATE TABLE IF NOT EXISTS event_outbox (
    event_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    aggregate_type VARCHAR(50) NOT NULL,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload JSONB NOT NULL,
    is_published BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP,

    CONSTRAINT chk_event_type_format CHECK (event_type ~ '^[A-Z_]+$')
);

CREATE INDEX idx_outbox_published ON event_outbox(is_published);
CREATE INDEX idx_outbox_created ON event_outbox(created_at);
CREATE INDEX idx_outbox_aggregate ON event_outbox(aggregate_type, aggregate_id);
CREATE INDEX idx_outbox_event_type ON event_outbox(event_type);

