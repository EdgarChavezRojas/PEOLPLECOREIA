-- Migración V2__Add_views_and_triggers.sql
-- Crear vistas y triggers para el módulo Workforce

-- Vista: Jerarquía completa de unidades organizativas
CREATE OR REPLACE VIEW org_unit_hierarchy_view AS
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
CREATE OR REPLACE VIEW available_positions AS
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

-- Trigger: Actualizar updated_at en org_unit
CREATE OR REPLACE FUNCTION update_org_unit_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER IF NOT EXISTS org_unit_updated_at_trigger
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

CREATE TRIGGER IF NOT EXISTS position_updated_at_trigger
BEFORE UPDATE ON position
FOR EACH ROW
EXECUTE FUNCTION update_position_timestamp();

-- Trigger: Actualizar updated_at en job
CREATE OR REPLACE FUNCTION update_job_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER IF NOT EXISTS job_updated_at_trigger
BEFORE UPDATE ON job
FOR EACH ROW
EXECUTE FUNCTION update_job_timestamp();

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

CREATE TRIGGER IF NOT EXISTS relationship_status_log_trigger
AFTER UPDATE ON relationship
FOR EACH ROW
-- ... existing code ...

-- Trigger: Registrar cambios de estado en STATUS_LOG
CREATE OR REPLACE FUNCTION log_relationship_status_change()
    RETURNS TRIGGER AS
$$
BEGIN
    IF NEW.current_status IS DISTINCT FROM OLD.current_status THEN
        INSERT INTO status_log (relationship_id, previous_status, new_status, changed_at)
        VALUES (NEW.relationship_id, OLD.current_status, NEW.current_status, CURRENT_DATE);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER IF NOT EXISTS relationship_status_log_trigger
    AFTER
UPDATE ON relationship
    FOR EACH ROW
EXECUTE FUNCTION log_relationship_status_change();

-- ... existing code ...;

