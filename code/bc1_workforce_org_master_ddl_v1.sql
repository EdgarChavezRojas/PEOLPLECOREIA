================================================================================
BC1: WORKFORCE & ORG MASTER - SQL DDL
Versión: v1.0
Fecha: 2026-04-24
Base de Datos: PostgreSQL 12+
================================================================================

================================================================================
SECUENCIA DE CREACIÓN - RESPETA DEPENDENCIAS FORÁNEAS
================================================================================

1. Crear extensiones necesarias
2. Crear tabla PERSON (sin dependencias)
3. Crear tabla ORG_UNIT (auto-referencial por parent_id)
4. Crear tabla JOB (sin dependencias)
5. Crear tabla POSITION (FK a org_unit, job)
6. Crear tabla RELATIONSHIP (FK a person)
7. Crear tablas de perfil (FK a relationship)
8. Crear tabla EVENT_OUTBOX (sin dependencias)

================================================================================
EXTENSIONES
================================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

================================================================================
TABLA: PERSON (Aggregate Root: PersonIdentity)
================================================================================

CREATE TABLE person (
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

================================================================================
TABLA: PARTY_IDENTIFIER (Entity dentro de PersonIdentity)
================================================================================

CREATE TABLE party_identifier (
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

================================================================================
TABLA: ORG_UNIT (Aggregate Root: OrgStructure)
================================================================================

CREATE TABLE org_unit (
    unit_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    parent_id UUID,  -- NULL si es raíz
    name VARCHAR(150) NOT NULL,
    unit_type VARCHAR(20) NOT NULL,  -- ADMINISTRATIVE, ACADEMIC, COMMERCIAL

    -- Embedded Value Object: CostCenter
    cost_code VARCHAR(50) NOT NULL,
    cost_description VARCHAR(255),

    geo_coords TEXT,  -- Para geocerca en Santa Cruz
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
CREATE INDEX idx_org_unit_name ON org_unit USING GIN (name gin_trgm_ops);  -- Para búsquedas textuales

================================================================================
TABLA: ORG_HIERARCHY (Entity dentro de OrgStructure)
================================================================================

CREATE TABLE org_hierarchy (
    hierarchy_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    parent_unit_id UUID NOT NULL,
    child_unit_id UUID NOT NULL,
    hierarchy_type VARCHAR(50),  -- Administrativa, Funcional, Académica
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

================================================================================
TABLA: JOB (Entity/VO: Descriptor de Cargos)
================================================================================

CREATE TABLE job (
    job_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    job_code VARCHAR(20) NOT NULL UNIQUE,
    title VARCHAR(150) NOT NULL,
    grade_band VARCHAR(10),  -- Banda salarial
    description TEXT,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_job_code ON job(job_code);
CREATE INDEX idx_job_title ON job USING GIN (title gin_trgm_ops);

================================================================================
TABLA: POSITION (Aggregate Root: PositionPlaza)
================================================================================

CREATE TABLE position (
    position_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    unit_id UUID NOT NULL,
    job_id UUID NOT NULL,
    pos_status VARCHAR(20) NOT NULL DEFAULT 'VACANT',  -- VACANT, OCCUPIED, RESERVED
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

================================================================================
TABLA: RELATIONSHIP (Aggregate Root: EmploymentRelationship)
================================================================================

CREATE TABLE relationship (
    relationship_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    person_id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    rel_type VARCHAR(20) NOT NULL,  -- LABOR, ACADEMIC, INTERNSHIP
    current_status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',  -- DRAFT, ACTIVE, SUSPENDED, TERMINATED
    hire_date DATE,

    created_at DATE NOT NULL DEFAULT CURRENT_DATE,
    updated_at DATE NOT NULL DEFAULT CURRENT_DATE,

    FOREIGN KEY (person_id) REFERENCES person(person_id) ON DELETE RESTRICT,
    UNIQUE(person_id, tenant_id, rel_type)
);

CREATE INDEX idx_relationship_person_id ON relationship(person_id);
CREATE INDEX idx_relationship_tenant_id ON relationship(tenant_id);
CREATE INDEX idx_relationship_status ON relationship(current_status);
CREATE INDEX idx_relationship_type ON relationship(rel_type);
CREATE INDEX idx_relationship_hire_date ON relationship(hire_date);

================================================================================
TABLA: WORKER_PROFILE (Entity dentro de Relationship)
================================================================================

CREATE TABLE worker_profile (
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

================================================================================
TABLA: ACADEMIC_PROFILE (Entity dentro de Relationship)
================================================================================

CREATE TABLE academic_profile (
    academic_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    relationship_id UUID NOT NULL UNIQUE,
    current_rank VARCHAR(30),  -- AUXILIAR, ADJUNTO, TITULAR, INVESTIGADOR
    teaching_load INTEGER,      -- Límite de carga horaria semestral

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (relationship_id) REFERENCES relationship(relationship_id) ON DELETE CASCADE,
    CONSTRAINT chk_teaching_load CHECK (teaching_load > 0)
);

CREATE INDEX idx_academic_profile_relationship_id ON academic_profile(relationship_id);
CREATE INDEX idx_academic_profile_rank ON academic_profile(current_rank);

================================================================================
TABLA: STATUS_LOG (Entity dentro de Relationship)
================================================================================

CREATE TABLE status_log (
    log_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    relationship_id UUID NOT NULL,
    previous_status VARCHAR(20),
    new_status VARCHAR(20) NOT NULL,
    change_reason TEXT,
    changed_at DATE NOT NULL DEFAULT CURRENT_DATE,
    changed_by UUID,  -- ID del usuario que realizó el cambio

    FOREIGN KEY (relationship_id) REFERENCES relationship(relationship_id) ON DELETE CASCADE,
    CONSTRAINT chk_status_different CHECK (previous_status IS DISTINCT FROM new_status)
);

CREATE INDEX idx_status_log_relationship_id ON status_log(relationship_id);
CREATE INDEX idx_status_log_changed_at ON status_log(changed_at);

================================================================================
TABLA: EVENT_OUTBOX (Patrón Outbox para Consistencia Eventual)
================================================================================

CREATE TABLE event_outbox (
    event_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    aggregate_type VARCHAR(50) NOT NULL,         -- Person, OrgUnit, Relationship, etc.
    aggregate_id UUID NOT NULL,                  -- ID de la entidad que generó el evento
    event_type VARCHAR(100) NOT NULL,            -- PERSON_CREATED, ORG_UNIT_CREATED, etc.
    payload JSONB NOT NULL,                      -- Datos del evento en JSON
    is_published BOOLEAN NOT NULL DEFAULT FALSE, -- Flag para saber si fue publicado

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP,

    CONSTRAINT chk_event_type_format CHECK (event_type ~ '^[A-Z_]+$')
);

CREATE INDEX idx_outbox_published ON event_outbox(is_published);
CREATE INDEX idx_outbox_created ON event_outbox(created_at);
CREATE INDEX idx_outbox_aggregate ON event_outbox(aggregate_type, aggregate_id);
CREATE INDEX idx_outbox_event_type ON event_outbox(event_type);

================================================================================
VISTAS Y PROCEDIMIENTOS AUXILIARES
================================================================================

-- Vista: Jerarquía completa de unidades organizativas
CREATE VIEW org_unit_hierarchy_view AS
WITH RECURSIVE hierarchy AS (
    SELECT
        unit_id,
        tenant_id,
        parent_id,
        name,
        unit_type,
        0 as level,
        CAST(unit_id AS TEXT) as path
    FROM org_unit
    WHERE parent_id IS NULL

    UNION ALL

    SELECT
        ou.unit_id,
        ou.tenant_id,
        ou.parent_id,
        ou.name,
        ou.unit_type,
        h.level + 1,
        h.path || '/' || CAST(ou.unit_id AS TEXT)
    FROM org_unit ou
    INNER JOIN hierarchy h ON ou.parent_id = h.unit_id
)
SELECT * FROM hierarchy;

-- Vista: Posiciones disponibles
CREATE VIEW available_positions AS
SELECT
    p.position_id,
    p.unit_id,
    p.job_id,
    p.max_slots,
    p.current_slots,
    (p.max_slots - p.current_slots) as available_slots,
    p.is_budgeted,
    j.title as job_title,
    ou.name as unit_name
FROM position p
INNER JOIN job j ON p.job_id = j.job_id
INNER JOIN org_unit ou ON p.unit_id = ou.unit_id
WHERE p.pos_status = 'VACANT' AND p.current_slots < p.max_slots;

================================================================================
TRIGGERS - AUDITORÍA AUTOMÁTICA
================================================================================

-- Trigger: Actualizar updated_at en org_unit
CREATE OR REPLACE FUNCTION update_org_unit_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER org_unit_updated_at_trigger
BEFORE UPDATE ON org_unit
FOR EACH ROW
EXECUTE FUNCTION update_org_unit_timestamp();

-- Trigger: Actualizar updated_at en position
CREATE OR REPLACE FUNCTION update_position_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER position_updated_at_trigger
BEFORE UPDATE ON position
FOR EACH ROW
EXECUTE FUNCTION update_position_timestamp();

-- Trigger: Registrar cambios de estado en STATUS_LOG
CREATE OR REPLACE FUNCTION log_relationship_status_change()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.current_status IS DISTINCT FROM OLD.current_status THEN
        INSERT INTO status_log (relationship_id, previous_status, new_status, changed_at)
        VALUES (NEW.relationship_id, OLD.current_status, NEW.current_status, CURRENT_DATE);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER relationship_status_log_trigger
AFTER UPDATE ON relationship
FOR EACH ROW
EXECUTE FUNCTION log_relationship_status_change();

================================================================================
SECUENCIAS DE DATOS DE PRUEBA (OPCIONAL)
================================================================================

-- Insertar tenant raíz (ajustar UUID según tu aplicación)
INSERT INTO org_unit (unit_id, tenant_id, parent_id, name, unit_type, cost_code, is_root)
VALUES ('550e8400-e29b-41d4-a716-446655440000', '550e8400-e29b-41d4-a716-446655440000', NULL, 'Solveria - Raíz', 'COMMERCIAL', 'ROOT-001', TRUE)
ON CONFLICT DO NOTHING;

-- Insertar jobs comunes
INSERT INTO job (job_id, job_code, title, grade_band)
VALUES
    ('550e8400-e29b-41d4-a716-446655440001', 'CAJERO', 'Cajero', 'BAND-A'),
    ('550e8400-e29b-41d4-a716-446655440002', 'GERENTE', 'Gerente de Tienda', 'BAND-B'),
    ('550e8400-e29b-41d4-a716-446655440003', 'DOCENTE', 'Docente', 'BAND-A')
ON CONFLICT DO NOTHING;

================================================================================
SENTENCIAS DE VALIDACIÓN POST-CREACIÓN
================================================================================

-- Validar que todas las tablas se crearon correctamente
SELECT
    schemaname,
    tablename
FROM pg_tables
WHERE schemaname = 'public'
AND tablename IN (
    'person', 'party_identifier', 'org_unit', 'org_hierarchy',
    'job', 'position', 'relationship', 'worker_profile',
    'academic_profile', 'status_log', 'event_outbox'
)
ORDER BY tablename;

-- Validar índices
SELECT
    schemaname,
    tablename,
    indexname
FROM pg_indexes
WHERE schemaname = 'public'
AND tablename IN (
    'person', 'party_identifier', 'org_unit', 'org_hierarchy',
    'job', 'position', 'relationship', 'worker_profile',
    'academic_profile', 'status_log', 'event_outbox'
)
ORDER BY tablename, indexname;

-- Validar vistas
SELECT
    schemaname,
    viewname
FROM pg_views
WHERE schemaname = 'public'
ORDER BY viewname;

================================================================================
SCRIPT DE ROLLBACK (Eliminar todas las tablas)
================================================================================

DROP TRIGGER IF EXISTS relationship_status_log_trigger ON relationship;
DROP TRIGGER IF EXISTS position_updated_at_trigger ON position;
DROP TRIGGER IF EXISTS org_unit_updated_at_trigger ON org_unit;

DROP FUNCTION IF EXISTS log_relationship_status_change();
DROP FUNCTION IF EXISTS update_position_timestamp();
DROP FUNCTION IF EXISTS update_org_unit_timestamp();

DROP VIEW IF EXISTS available_positions;
DROP VIEW IF EXISTS org_unit_hierarchy_view;

DROP TABLE IF EXISTS event_outbox CASCADE;
DROP TABLE IF EXISTS status_log CASCADE;
DROP TABLE IF EXISTS academic_profile CASCADE;
DROP TABLE IF EXISTS worker_profile CASCADE;
DROP TABLE IF EXISTS relationship CASCADE;
DROP TABLE IF EXISTS position CASCADE;
DROP TABLE IF EXISTS job CASCADE;
DROP TABLE IF EXISTS org_hierarchy CASCADE;
DROP TABLE IF EXISTS org_unit CASCADE;
DROP TABLE IF EXISTS party_identifier CASCADE;
DROP TABLE IF EXISTS person CASCADE;

================================================================================
PROPIEDADES DE CONFIGURACIÓN HIBERNATE (para esquema auto)
================================================================================

# En application.yml, para DDL automático (SOLO EN DESARROLLO):
# spring.jpa.hibernate.ddl-auto: create
# spring.jpa.show-sql: true
# spring.jpa.properties.hibernate.format_sql: true

# En PRODUCCIÓN, usar:
# spring.jpa.hibernate.ddl-auto: validate

================================================================================
FIN - BC1 WORKFORCE & ORG MASTER - SQL DDL v1.0
================================================================================

