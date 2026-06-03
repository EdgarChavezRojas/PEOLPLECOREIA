-- Flyway Migration: V20260524190500__seed_remaining_domains
-- Description: Seed baseline workforce and remaining domains (Legal, Financial, Accruals, Dossier, Scheduling, Experience, Core/Audit)
-- Simulation Period: Jan-May 2026
-- Simulation Base Date: '2026-05-24 09:00:00'
-- Tenant ID: 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b'

--------------------------------------------------------------------------------
-- 0. Core Workforce Baseline (Pre-requisites for Foreign Keys)
--------------------------------------------------------------------------------
-- Inserts para tabla tenant
INSERT INTO tenant (tenant_id, name, status, description, created_at, updated_at) VALUES
    ('e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', 'Solveria', 'ACTIVE', 'Tenant principal del sistema', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO tenant (tenant_id, name, status, description, created_at, updated_at) VALUES
    ('b6f9c2d4-2a1e-4f3b-9c8e-1a2b3c4d5e6f', 'Acme Corp', 'ACTIVE', 'Cliente corporativo ACME', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO tenant (tenant_id, name, status, description, created_at, updated_at) VALUES
    ('c3d4e5f6-7a8b-4c9d-0e1f-2a3b4c5d6e7f', 'Beta Labs', 'INACTIVE', 'Entorno de pruebas Beta', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO tenant (tenant_id, name, status, description, created_at, updated_at) VALUES
    ('d4e5f6a7-8b9c-4d0e-1f2a-3b4c5d6e7f8a', 'Dev Sandbox', 'ACTIVE', 'Tenant para desarrolladores', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO iam_user (id, version, tenant_id, created_at, created_by, last_modified_at, last_modified_by, username, email, password, active) VALUES
    (4, 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', 'juan_perez', 'juan.perez@solveria.com', '$2a$10$CHyru/epuPrjjg73Ghb5TeQDgIdKhQM1TNSGK500R8e2USUVPFSDW', TRUE),
    (5, 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', 'ana_rojas', 'ana.rojas@solveria.com', '$2a$10$CHyru/epuPrjjg73Ghb5TeQDgIdKhQM1TNSGK500R8e2USUVPFSDW', TRUE)
    ON CONFLICT (id) DO NOTHING;

INSERT INTO iam_user_roles (user_id, role_id) VALUES 
    (4, 2), 
    (5, 2)
    ON CONFLICT DO NOTHING;

INSERT INTO org_unit (unit_id, version, tenant_id, created_at, created_by, last_modified_at, last_modified_by, parent_id, name, unit_type, cost_code, description, geo_coords, is_root) VALUES
    ('b2b00000-0000-4000-8000-000000000001', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', NULL, 'Solveria', 'COMPANY', 'CC-ROOT', 'Root organization', NULL, TRUE),
    ('b2b00000-0000-4000-8000-000000000002', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', 'b2b00000-0000-4000-8000-000000000001', 'Operations', 'DEPARTMENT', 'CC-OPS', 'Operations and delivery', NULL, FALSE)
    ON CONFLICT (unit_id) DO NOTHING;

INSERT INTO org_hierarchy (hierarchy_id, version, tenant_id, created_at, created_by, last_modified_at, last_modified_by, parent_unit_id, child_unit_id, hierarchy_type, effective_date, end_date) VALUES
    ('b2b00000-0000-4000-8000-000000000003', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', 'b2b00000-0000-4000-8000-000000000001', 'b2b00000-0000-4000-8000-000000000002', 'LINE', '2026-05-01', NULL)
    ON CONFLICT (hierarchy_id) DO NOTHING;

INSERT INTO job (job_id, version, tenant_id, created_at, created_by, last_modified_at, last_modified_by, job_code, title, grade_band, description) VALUES
    ('b3b00000-0000-4000-8000-000000000001', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', 'ENG-001', 'Software Engineer', 'G6', 'Engineering role'),
    ('b3b00000-0000-4000-8000-000000000002', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', 'HR-001', 'HR Analyst', 'G5', 'Human Resources role')
    ON CONFLICT (job_id) DO NOTHING;

INSERT INTO position (position_id, version, tenant_id, created_at, created_by, last_modified_at, last_modified_by, unit_id, job_id, pos_status, is_budgeted, max_slots, current_slots) VALUES
    ('b4b00000-0000-4000-8000-000000000001', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', 'b2b00000-0000-4000-8000-000000000002', 'b3b00000-0000-4000-8000-000000000001', 'FILLED', TRUE, 3, 3),
    ('b4b00000-0000-4000-8000-000000000002', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', 'b2b00000-0000-4000-8000-000000000002', 'b3b00000-0000-4000-8000-000000000002', 'FILLED', TRUE, 1, 1)
    ON CONFLICT (position_id) DO NOTHING;

INSERT INTO person (person_id, version, tenant_id, created_at, created_by, last_modified_at, last_modified_by, first_name, last_name, birth_date, gender, marital_status, profession_title, DNI, user_id, global_id, email, phone, address) VALUES
    ('8f9a6f1a-2a1d-4a61-9a1b-100000000001', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', 'Admin', 'Platform', '1985-01-12', 'M', 'SINGLE', 'Systems Administrator', 'CI-450001', 1, 'G-0001', 'admin.platform@solveria.com', '+59170000001', 'Av. Central 100'),
    ('8f9a6f1a-2a1d-4a61-9a1b-100000000002', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', 'Carlos', 'Mendoza', '1990-03-18', 'M', 'MARRIED', 'Software Engineer', 'CI-450002', 2, 'G-0002', 'carlos.mendoza@solveria.com', '+59170000002', 'Calle 12 #45'),
    ('8f9a6f1a-2a1d-4a61-9a1b-100000000003', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', 'Maria', 'Gomez', '1992-07-09', 'F', 'SINGLE', 'HR Analyst', 'CI-450003', 3, 'G-0003', 'maria.gomez@solveria.com', '+59170000003', 'Av. Norte 230'),
    ('8f9a6f1a-2a1d-4a61-9a1b-100000000004', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', 'Juan', 'Perez', '1988-11-22', 'M', 'MARRIED', 'Software Engineer', 'CI-450004', 4, 'G-0004', 'juan.perez@solveria.com', '+59170000004', 'Calle 7 #18'),
    ('8f9a6f1a-2a1d-4a61-9a1b-100000000005', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', 'Ana', 'Rojas', '1995-02-04', 'F', 'SINGLE', 'Software Engineer', 'CI-450005', 5, 'G-0005', 'ana.rojas@solveria.com', '+59170000005', 'Av. Sur 88')
    ON CONFLICT (person_id) DO NOTHING;

INSERT INTO party_identifier (identifier_id, version, tenant_id, created_at, created_by, last_modified_at, last_modified_by, person_id, id_type, id_number, extension, issue_date, expiry_date) VALUES
    ('9aa00000-0000-4000-8000-000000000001', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '8f9a6f1a-2a1d-4a61-9a1b-100000000001', 'CI', 'CI-450001', 'LP', '2015-02-01', '2030-02-01'),
    ('9aa00000-0000-4000-8000-000000000002', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '8f9a6f1a-2a1d-4a61-9a1b-100000000002', 'CI', 'CI-450002', 'SC', '2016-04-12', '2031-04-12'),
    ('9aa00000-0000-4000-8000-000000000003', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '8f9a6f1a-2a1d-4a61-9a1b-100000000003', 'CI', 'CI-450003', 'CB', '2017-05-08', '2032-05-08'),
    ('9aa00000-0000-4000-8000-000000000004', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '8f9a6f1a-2a1d-4a61-9a1b-100000000004', 'CI', 'CI-450004', 'LP', '2014-08-20', '2029-08-20'),
    ('9aa00000-0000-4000-8000-000000000005', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '8f9a6f1a-2a1d-4a61-9a1b-100000000005', 'CI', 'CI-450005', 'SC', '2019-01-15', '2034-01-15')
    ON CONFLICT (identifier_id) DO NOTHING;

INSERT INTO relationship (relationship_id, version, tenant_id, created_at, created_by, last_modified_at, last_modified_by, person_id, rel_type, current_status, hire_date) VALUES
    ('9f9a6f1a-2a1d-4a61-9a1b-200000000002', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '8f9a6f1a-2a1d-4a61-9a1b-100000000002', 'EMPLOYEE', 'ACTIVE', '2024-02-15'),
    ('9f9a6f1a-2a1d-4a61-9a1b-200000000003', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '8f9a6f1a-2a1d-4a61-9a1b-100000000003', 'EMPLOYEE', 'ACTIVE', '2023-10-01'),
    ('9f9a6f1a-2a1d-4a61-9a1b-200000000004', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '8f9a6f1a-2a1d-4a61-9a1b-100000000004', 'EMPLOYEE', 'ACTIVE', '2022-06-20'),
    ('9f9a6f1a-2a1d-4a61-9a1b-200000000005', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '8f9a6f1a-2a1d-4a61-9a1b-100000000005', 'EMPLOYEE', 'ACTIVE', '2025-01-10')
    ON CONFLICT (relationship_id) DO NOTHING;

INSERT INTO worker_profile (profile_id, version, tenant_id, created_at, created_by, last_modified_at, last_modified_by, relationship_id, employee_no, department, job_title) VALUES
    ('a1a60000-0000-4000-8000-000000000002', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '9f9a6f1a-2a1d-4a61-9a1b-200000000002', 'EMP-1002', 'Engineering', 'Software Engineer'),
    ('a1a60000-0000-4000-8000-000000000003', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '9f9a6f1a-2a1d-4a61-9a1b-200000000003', 'EMP-1003', 'Human Resources', 'HR Analyst'),
    ('a1a60000-0000-4000-8000-000000000004', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '9f9a6f1a-2a1d-4a61-9a1b-200000000004', 'EMP-1004', 'Engineering', 'Software Engineer'),
    ('a1a60000-0000-4000-8000-000000000005', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '9f9a6f1a-2a1d-4a61-9a1b-200000000005', 'EMP-1005', 'Engineering', 'Software Engineer')
    ON CONFLICT (profile_id) DO NOTHING;

INSERT INTO holiday_calendar (holiday_id, version, tenant_id, created_at, created_by, last_modified_at, last_modified_by, holiday_date, scope) VALUES
    ('c5c00000-0000-4000-8000-000000000001', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-01', 'NATIONAL')
    ON CONFLICT (holiday_id) DO NOTHING;


--------------------------------------------------------------------------------
-- 1. Dominio Legal
--------------------------------------------------------------------------------

-- Seed legal_policy_rule
INSERT INTO legal_policy_rule (policy_id, version, tenant_id, created_at, created_by, last_modified_at, last_modified_by, policy_name, description) VALUES
    ('c1a00000-0000-4000-8000-000000000100', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', 'SMN', 'Salario Mínimo Nacional')
    ON CONFLICT (policy_id) DO NOTHING;

-- Seed legal_policy_threshold
INSERT INTO legal_policy_threshold (policy_id, threshold_value, effective_date)
SELECT 'c1a00000-0000-4000-8000-000000000100', 3300.0000, '2026-01-01'
WHERE NOT EXISTS (
    SELECT 1 FROM legal_policy_threshold
    WHERE policy_id = 'c1a00000-0000-4000-8000-000000000100'
);

-- Seed legal_contract (1 per employee, status=APPROVED, type=INDEFINIDO)
INSERT INTO legal_contract (contract_id, version, tenant_id, created_at, created_by, last_modified_at, last_modified_by, relationship_id, contract_type, status, project_id, employment_cond, tacita_reconduccion_alert_sent) VALUES
    -- Carlos Mendoza
    ('c1a00000-0000-4000-8000-000000000002', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '9f9a6f1a-2a1d-4a61-9a1b-200000000002', 'INDEFINIDO', 'APPROVED', 'f1f00000-0000-4000-8000-000000000001', 'TC', FALSE),
    -- Maria Gomez
    ('c1a00000-0000-4000-8000-000000000003', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '9f9a6f1a-2a1d-4a61-9a1b-200000000003', 'INDEFINIDO', 'APPROVED', 'f1f00000-0000-4000-8000-000000000001', 'TC', FALSE),
    -- Juan Perez
    ('c1a00000-0000-4000-8000-000000000004', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '9f9a6f1a-2a1d-4a61-9a1b-200000000004', 'INDEFINIDO', 'APPROVED', 'f1f00000-0000-4000-8000-000000000001', 'TC', FALSE),
    -- Ana Rojas
    ('c1a00000-0000-4000-8000-000000000005', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '9f9a6f1a-2a1d-4a61-9a1b-200000000005', 'INDEFINIDO', 'APPROVED', 'f1f00000-0000-4000-8000-000000000001', 'TC', FALSE)
    ON CONFLICT (contract_id) DO NOTHING;

-- Seed legal_contract_addendum (1 per contract, effective_from=hire_date, status=APPROVED)
INSERT INTO legal_contract_addendum (addendum_id, version, tenant_id, created_at, created_by, last_modified_at, last_modified_by, contract_id, status, effective_from, effective_to, basic_salary, total_earned_proj, net_salary_proj, currency, smn_applied, tax_regime, infocal_active) VALUES
    -- Carlos Mendoza (Hire: 2024-02-15, Salary: 7000)
    ('c1a00000-0000-4000-8000-000000000012', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', 'c1a00000-0000-4000-8000-000000000002', 'APPROVED', '2024-02-15', '2029-12-31', 7000.00, 7000.00, 6110.30, 'BOB', 3300.00, 'REGULAR', TRUE),
    -- Maria Gomez (Hire: 2023-10-01, Salary: 6500)
    ('c1a00000-0000-4000-8000-000000000013', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', 'c1a00000-0000-4000-8000-000000000003', 'APPROVED', '2023-10-01', '2029-12-31', 6500.00, 6500.00, 5673.85, 'BOB', 3300.00, 'REGULAR', TRUE),
    -- Juan Perez (Hire: 2022-06-20, Salary: 6000)
    ('c1a00000-0000-4000-8000-000000000014', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', 'c1a00000-0000-4000-8000-000000000004', 'APPROVED', '2022-06-20', '2029-12-31', 6000.00, 6000.00, 5237.40, 'BOB', 3300.00, 'REGULAR', TRUE),
    -- Ana Rojas (Hire: 2025-01-10, Salary: 6800)
    ('c1a00000-0000-4000-8000-000000000015', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', 'c1a00000-0000-4000-8000-000000000005', 'APPROVED', '2025-01-10', '2029-12-31', 6800.00, 6800.00, 5935.72, 'BOB', 3300.00, 'REGULAR', TRUE)
    ON CONFLICT (addendum_id) DO NOTHING;


--------------------------------------------------------------------------------
-- 2. Dominio Financial
--------------------------------------------------------------------------------

-- Seed funding_source (Proyecto Principal, vinculando los contratos a traves de project_id)
INSERT INTO funding_source (source_id, version, tenant_id, created_at, created_by, last_modified_at, last_modified_by, project_code, total_budget, available_budget, burn_rate, created_by_user) VALUES
    ('f1f00000-0000-4000-8000-000000000001', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', 'PROY-PRINCIPAL', 500000.00, 500000.00, 0.00, 'SYSTEM')
    ON CONFLICT (source_id) DO NOTHING;

-- Seed social_security_account (gestora rate 0.1271)
INSERT INTO social_security_account (ssa_id, version, tenant_id, created_at, created_by, last_modified_at, last_modified_by, person_id, gestora_code, contribution_rate, last_contribution, created_by_user) VALUES
    -- Carlos Mendoza (Person ID: 8f9a6f1a-2a1d-4a61-9a1b-100000000002)
    ('f1f00000-0000-4000-8000-000000000012', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '8f9a6f1a-2a1d-4a61-9a1b-100000000002', 'GES-1002', 0.1271, '2026-05-24', 'SYSTEM'),
    -- Maria Gomez (Person ID: 8f9a6f1a-2a1d-4a61-9a1b-100000000003)
    ('f1f00000-0000-4000-8000-000000000013', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '8f9a6f1a-2a1d-4a61-9a1b-100000000003', 'GES-1003', 0.1271, '2026-05-24', 'SYSTEM'),
    -- Juan Perez (Person ID: 8f9a6f1a-2a1d-4a61-9a1b-100000000004)
    ('f1f00000-0000-4000-8000-000000000014', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '8f9a6f1a-2a1d-4a61-9a1b-100000000004', 'GES-1004', 0.1271, '2026-05-24', 'SYSTEM'),
    -- Ana Rojas (Person ID: 8f9a6f1a-2a1d-4a61-9a1b-100000000005)
    ('f1f00000-0000-4000-8000-000000000015', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '8f9a6f1a-2a1d-4a61-9a1b-100000000005', 'GES-1005', 0.1271, '2026-05-24', 'SYSTEM')
    ON CONFLICT (ssa_id) DO NOTHING;

-- Seed tax_form_110 (Jan-May 2026, total_declared=900.00, verified_credit=117.00 which is 13% of 900.00, to justify ~117 BOB retention)
INSERT INTO tax_form_110 (form_id, version, tenant_id, created_at, created_by, last_modified_at, last_modified_by, person_id, total_declared, verified_credit, doc_id, period_year, period_month, created_by_user) VALUES
    -- Carlos Mendoza (Person ID: 8f9a6f1a-2a1d-4a61-9a1b-100000000002)
    ('f1f00000-0000-4000-8000-000000000101', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '8f9a6f1a-2a1d-4a61-9a1b-100000000002', 900.00, 117.00, NULL, 2026, 1, 'SYSTEM'),
    ('f1f00000-0000-4000-8000-000000000102', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '8f9a6f1a-2a1d-4a61-9a1b-100000000002', 900.00, 117.00, NULL, 2026, 2, 'SYSTEM'),
    ('f1f00000-0000-4000-8000-000000000103', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '8f9a6f1a-2a1d-4a61-9a1b-100000000002', 900.00, 117.00, NULL, 2026, 3, 'SYSTEM'),
    ('f1f00000-0000-4000-8000-000000000104', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '8f9a6f1a-2a1d-4a61-9a1b-100000000002', 900.00, 117.00, NULL, 2026, 4, 'SYSTEM'),
    ('f1f00000-0000-4000-8000-000000000105', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '8f9a6f1a-2a1d-4a61-9a1b-100000000002', 900.00, 117.00, NULL, 2026, 5, 'SYSTEM'),
    -- Maria Gomez (Person ID: 8f9a6f1a-2a1d-4a61-9a1b-100000000003)
    ('f1f00000-0000-4000-8000-000000000111', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '8f9a6f1a-2a1d-4a61-9a1b-100000000003', 900.00, 117.00, NULL, 2026, 1, 'SYSTEM'),
    ('f1f00000-0000-4000-8000-000000000112', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '8f9a6f1a-2a1d-4a61-9a1b-100000000003', 900.00, 117.00, NULL, 2026, 2, 'SYSTEM'),
    ('f1f00000-0000-4000-8000-000000000113', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '8f9a6f1a-2a1d-4a61-9a1b-100000000003', 900.00, 117.00, NULL, 2026, 3, 'SYSTEM'),
    ('f1f00000-0000-4000-8000-000000000114', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '8f9a6f1a-2a1d-4a61-9a1b-100000000003', 900.00, 117.00, NULL, 2026, 4, 'SYSTEM'),
    ('f1f00000-0000-4000-8000-000000000115', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '8f9a6f1a-2a1d-4a61-9a1b-100000000003', 900.00, 117.00, NULL, 2026, 5, 'SYSTEM'),
    -- Juan Perez (Person ID: 8f9a6f1a-2a1d-4a61-9a1b-100000000004)
    ('f1f00000-0000-4000-8000-000000000121', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '8f9a6f1a-2a1d-4a61-9a1b-100000000004', 900.00, 117.00, NULL, 2026, 1, 'SYSTEM'),
    ('f1f00000-0000-4000-8000-000000000122', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '8f9a6f1a-2a1d-4a61-9a1b-100000000004', 900.00, 117.00, NULL, 2026, 2, 'SYSTEM'),
    ('f1f00000-0000-4000-8000-000000000123', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '8f9a6f1a-2a1d-4a61-9a1b-100000000004', 900.00, 117.00, NULL, 2026, 3, 'SYSTEM'),
    ('f1f00000-0000-4000-8000-000000000124', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '8f9a6f1a-2a1d-4a61-9a1b-100000000004', 900.00, 117.00, NULL, 2026, 4, 'SYSTEM'),
    ('f1f00000-0000-4000-8000-000000000125', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '8f9a6f1a-2a1d-4a61-9a1b-100000000004', 900.00, 117.00, NULL, 2026, 5, 'SYSTEM'),
    -- Ana Rojas (Person ID: 8f9a6f1a-2a1d-4a61-9a1b-100000000005)
    ('f1f00000-0000-4000-8000-000000000131', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '8f9a6f1a-2a1d-4a61-9a1b-100000000005', 900.00, 117.00, NULL, 2026, 1, 'SYSTEM'),
    ('f1f00000-0000-4000-8000-000000000132', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '8f9a6f1a-2a1d-4a61-9a1b-100000000005', 900.00, 117.00, NULL, 2026, 2, 'SYSTEM'),
    ('f1f00000-0000-4000-8000-000000000133', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '8f9a6f1a-2a1d-4a61-9a1b-100000000005', 900.00, 117.00, NULL, 2026, 3, 'SYSTEM'),
    ('f1f00000-0000-4000-8000-000000000134', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '8f9a6f1a-2a1d-4a61-9a1b-100000000005', 900.00, 117.00, NULL, 2026, 4, 'SYSTEM'),
    ('f1f00000-0000-4000-8000-000000000135', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '8f9a6f1a-2a1d-4a61-9a1b-100000000005', 900.00, 117.00, NULL, 2026, 5, 'SYSTEM')
    ON CONFLICT (form_id) DO NOTHING;


--------------------------------------------------------------------------------
-- 3. Dominio Accruals
--------------------------------------------------------------------------------

-- Seed accrual_balance (Vacations, 15 days per completed year)
INSERT INTO accrual_balance (balance_id, version, tenant_id, created_at, created_by, last_modified_at, last_modified_by, relationship_id, balance_type, unit, current_balance, initial_balance, days_accrued_ytd, days_taken_ytd, last_accrual_date) VALUES
    -- Carlos Mendoza (Hire: 2024-02-15, 2 full years completed -> 30 days)
    ('e1e00000-0000-4000-8000-000000000002', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '9f9a6f1a-2a1d-4a61-9a1b-200000000002', 'VACATION', 'DAYS', 30.00, 30.00, 30.00, 0.00, '2026-05-24'),
    -- Maria Gomez (Hire: 2023-10-01, 2 full years completed -> 30 days)
    ('e1e00000-0000-4000-8000-000000000003', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '9f9a6f1a-2a1d-4a61-9a1b-200000000003', 'VACATION', 'DAYS', 30.00, 30.00, 30.00, 0.00, '2026-05-24'),
    -- Juan Perez (Hire: 2022-06-20, 3 full years completed -> 45 days)
    ('e1e00000-0000-4000-8000-000000000004', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '9f9a6f1a-2a1d-4a61-9a1b-200000000004', 'VACATION', 'DAYS', 45.00, 45.00, 45.00, 0.00, '2026-05-24'),
    -- Ana Rojas (Hire: 2025-01-10, 1 full year completed -> 15 days)
    ('e1e00000-0000-4000-8000-000000000005', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '9f9a6f1a-2a1d-4a61-9a1b-200000000005', 'VACATION', 'DAYS', 15.00, 15.00, 15.00, 0.00, '2026-05-24')
    ON CONFLICT (balance_id) DO NOTHING;

-- Seed quinquenio_provision (8.33% of base salary per completed month)
INSERT INTO quinquenio_provision (provision_id, version, tenant_id, created_at, created_by, last_modified_at, last_modified_by, relationship_id, total_accumulated, penalty_active) VALUES
    -- Carlos Mendoza (7000 * 0.0833 * 27 months = 15743.70)
    ('e1e00000-0000-4000-8000-000000000012', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '9f9a6f1a-2a1d-4a61-9a1b-200000000002', 15743.70, FALSE),
    -- Maria Gomez (6500 * 0.0833 * 31 months = 16784.95)
    ('e1e00000-0000-4000-8000-000000000013', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '9f9a6f1a-2a1d-4a61-9a1b-200000000003', 16784.95, FALSE),
    -- Juan Perez (6000 * 0.0833 * 47 months = 23490.60)
    ('e1e00000-0000-4000-8000-000000000014', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '9f9a6f1a-2a1d-4a61-9a1b-200000000004', 23490.60, FALSE),
    -- Ana Rojas (6800 * 0.0833 * 16 months = 9063.04)
    ('e1e00000-0000-4000-8000-000000000015', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '9f9a6f1a-2a1d-4a61-9a1b-200000000005', 9063.04, FALSE)
    ON CONFLICT (provision_id) DO NOTHING;

-- Seed seniority_milestone (For Juan Perez, who has over 2 years, linking to vacation balance id, months=24, BASE_3_SMN)
INSERT INTO seniority_milestone (milestone_id, version, tenant_id, created_at, created_by, last_modified_at, last_modified_by, balance_id, months_completed, base_smn_type) VALUES
    ('e1e00000-0000-4000-8000-000000000024', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', 'e1e00000-0000-4000-8000-000000000004', 24, 'BASE_3_SMN')
    ON CONFLICT (milestone_id) DO NOTHING;

-- Seed benefit_accrual (Aguinaldo & Prima de Utilidad for fiscal year 2026, 5 months accrued: salary * 0.0833 * 5)
INSERT INTO benefit_accrual (benefit_id, version, tenant_id, created_at, created_by, last_modified_at, last_modified_by, relationship_id, benefit_type, fiscal_year, accrued_amount) VALUES
    -- Carlos Mendoza (7000 * 0.4165 = 2915.50)
    ('e1e00000-0000-4000-8000-000000000032', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '9f9a6f1a-2a1d-4a61-9a1b-200000000002', 'AGUINALDO', 2026, 2915.50),
    ('e1e00000-0000-4000-8000-000000000042', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '9f9a6f1a-2a1d-4a61-9a1b-200000000002', 'PRIMA_UTILIDAD', 2026, 2915.50),
    -- Maria Gomez (6500 * 0.4165 = 2707.25)
    ('e1e00000-0000-4000-8000-000000000033', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '9f9a6f1a-2a1d-4a61-9a1b-200000000003', 'AGUINALDO', 2026, 2707.25),
    ('e1e00000-0000-4000-8000-000000000043', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '9f9a6f1a-2a1d-4a61-9a1b-200000000003', 'PRIMA_UTILIDAD', 2026, 2707.25),
    -- Juan Perez (6000 * 0.4165 = 2499.00)
    ('e1e00000-0000-4000-8000-000000000034', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '9f9a6f1a-2a1d-4a61-9a1b-200000000004', 'AGUINALDO', 2026, 2499.00),
    ('e1e00000-0000-4000-8000-000000000044', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '9f9a6f1a-2a1d-4a61-9a1b-200000000004', 'PRIMA_UTILIDAD', 2026, 2499.00),
    -- Ana Rojas (6800 * 0.4165 = 2832.20)
    ('e1e00000-0000-4000-8000-000000000035', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '9f9a6f1a-2a1d-4a61-9a1b-200000000005', 'AGUINALDO', 2026, 2832.20),
    ('e1e00000-0000-4000-8000-000000000045', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '9f9a6f1a-2a1d-4a61-9a1b-200000000005', 'PRIMA_UTILIDAD', 2026, 2832.20)
    ON CONFLICT (benefit_id) DO NOTHING;


--------------------------------------------------------------------------------
-- 4. Dominio Dossier
--------------------------------------------------------------------------------

-- Seed talent_inventory
INSERT INTO talent_inventory (inventory_id, version, tenant_id, created_at, created_by, last_modified_at, last_modified_by, relationship_id) VALUES
    ('d4e00000-0000-4000-8000-000000000002', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '9f9a6f1a-2a1d-4a61-9a1b-200000000002'),
    ('d4e00000-0000-4000-8000-000000000003', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '9f9a6f1a-2a1d-4a61-9a1b-200000000003'),
    ('d4e00000-0000-4000-8000-000000000004', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '9f9a6f1a-2a1d-4a61-9a1b-200000000004'),
    ('d4e00000-0000-4000-8000-000000000005', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '9f9a6f1a-2a1d-4a61-9a1b-200000000005')
    ON CONFLICT (inventory_id) DO NOTHING;

-- Seed skill_set
INSERT INTO skill_set (skill_id, version, tenant_id, created_at, created_by, last_modified_at, last_modified_by, skill_name, proficiency, inventory_id) VALUES
    -- Carlos Mendoza
    ('d4e00000-0000-4000-8000-000000000101', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', 'Java', 'ADVANCED', 'd4e00000-0000-4000-8000-000000000002'),
    ('d4e00000-0000-4000-8000-000000000102', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', 'React', 'ADVANCED', 'd4e00000-0000-4000-8000-000000000002'),
    ('d4e00000-0000-4000-8000-000000000103', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', 'PostgreSQL', 'ADVANCED', 'd4e00000-0000-4000-8000-000000000002'),
    -- Maria Gomez
    ('d4e00000-0000-4000-8000-000000000104', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', 'Recruitment', 'ADVANCED', 'd4e00000-0000-4000-8000-000000000003'),
    ('d4e00000-0000-4000-8000-000000000105', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', 'Payroll', 'ADVANCED', 'd4e00000-0000-4000-8000-000000000003'),
    ('d4e00000-0000-4000-8000-000000000106', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', 'Labor Law', 'ADVANCED', 'd4e00000-0000-4000-8000-000000000003'),
    -- Juan Perez
    ('d4e00000-0000-4000-8000-000000000107', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', 'Java', 'ADVANCED', 'd4e00000-0000-4000-8000-000000000004'),
    ('d4e00000-0000-4000-8000-000000000108', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', 'C#', 'ADVANCED', 'd4e00000-0000-4000-8000-000000000004'),
    ('d4e00000-0000-4000-8000-000000000109', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', 'PostgreSQL', 'ADVANCED', 'd4e00000-0000-4000-8000-000000000004'),
    -- Ana Rojas
    ('d4e00000-0000-4000-8000-000000000110', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', 'Java', 'ADVANCED', 'd4e00000-0000-4000-8000-000000000005'),
    ('d4e00000-0000-4000-8000-000000000111', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', 'React', 'ADVANCED', 'd4e00000-0000-4000-8000-000000000005')
    ON CONFLICT (skill_id) DO NOTHING;

-- Seed assigned_asset (1 LAPTOP and 1 MONITOR per employee. Category='COMPUTERS' because Java AssetCategory only contains COMPUTERS, TOOLS, FURNITURE, OTHER. Status='CUSTODY' to map to Java AssetStatus.CUSTODY)
INSERT INTO assigned_asset (assignment_id, version, tenant_id, created_at, created_by, last_modified_at, last_modified_by, worker_id, asset_tag, status, assigned_at, returned_at, category, tech_specs, initial_state) VALUES
    -- Carlos Mendoza
    ('d4a00000-0000-4000-8000-000000000002', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '9f9a6f1a-2a1d-4a61-9a1b-200000000002', 'TAG-LAP-1002', 'CUSTODY', '2026-05-24 09:00:00', NULL, 'COMPUTERS', '{"brand": "Dell", "ram": "16GB", "type": "LAPTOP"}'::jsonb, 'NEW'),
    ('d4a00000-0000-4000-8000-000000000012', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '9f9a6f1a-2a1d-4a61-9a1b-200000000002', 'TAG-MON-1002', 'CUSTODY', '2026-05-24 09:00:00', NULL, 'COMPUTERS', '{"brand": "Dell", "size": "27 inch", "type": "MONITOR"}'::jsonb, 'NEW'),
    -- Maria Gomez
    ('d4a00000-0000-4000-8000-000000000003', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '9f9a6f1a-2a1d-4a61-9a1b-200000000003', 'TAG-LAP-1003', 'CUSTODY', '2026-05-24 09:00:00', NULL, 'COMPUTERS', '{"brand": "Dell", "ram": "16GB", "type": "LAPTOP"}'::jsonb, 'NEW'),
    ('d4a00000-0000-4000-8000-000000000013', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '9f9a6f1a-2a1d-4a61-9a1b-200000000003', 'TAG-MON-1003', 'CUSTODY', '2026-05-24 09:00:00', NULL, 'COMPUTERS', '{"brand": "Dell", "size": "27 inch", "type": "MONITOR"}'::jsonb, 'NEW'),
    -- Juan Perez
    ('d4a00000-0000-4000-8000-000000000004', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '9f9a6f1a-2a1d-4a61-9a1b-200000000004', 'TAG-LAP-1004', 'CUSTODY', '2026-05-24 09:00:00', NULL, 'COMPUTERS', '{"brand": "Dell", "ram": "16GB", "type": "LAPTOP"}'::jsonb, 'NEW'),
    ('d4a00000-0000-4000-8000-000000000014', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '9f9a6f1a-2a1d-4a61-9a1b-200000000004', 'TAG-MON-1004', 'CUSTODY', '2026-05-24 09:00:00', NULL, 'COMPUTERS', '{"brand": "Dell", "size": "27 inch", "type": "MONITOR"}'::jsonb, 'NEW'),
    -- Ana Rojas
    ('d4a00000-0000-4000-8000-000000000005', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '9f9a6f1a-2a1d-4a61-9a1b-200000000005', 'TAG-LAP-1005', 'CUSTODY', '2026-05-24 09:00:00', NULL, 'COMPUTERS', '{"brand": "Dell", "ram": "16GB", "type": "LAPTOP"}'::jsonb, 'NEW'),
    ('d4a00000-0000-4000-8000-000000000015', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '9f9a6f1a-2a1d-4a61-9a1b-200000000005', 'TAG-MON-1005', 'CUSTODY', '2026-05-24 09:00:00', NULL, 'COMPUTERS', '{"brand": "Dell", "size": "27 inch", "type": "MONITOR"}'::jsonb, 'NEW')
    ON CONFLICT (assignment_id) DO NOTHING;

-- Seed document_record (Current state='APPROVED' because Java ValidationState only allows PENDING, APPROVED, REJECTED, EXPIRED)
INSERT INTO document_record (doc_id, version, tenant_id, created_at, created_by, last_modified_at, last_modified_by, relationship_id, doc_category, doc_type, is_critical, current_state, reviewer_id, review_date, reject_reason, storage_id, file_name, hash_sha256, expiry_date, expiration_warning_sent) VALUES
    -- Carlos Mendoza
    ('d4d00000-0000-4000-8000-000000000002', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '9f9a6f1a-2a1d-4a61-9a1b-200000000002', 'LEGAL', 'EVIDENCIA_CONTRATO_WORM', TRUE, 'APPROVED', '8f9a6f1a-2a1d-4a61-9a1b-100000000001', '2026-05-24 09:00:00', NULL, '00000000-0000-4000-8000-000000000001', 'contrato_carlos.pdf', 'hash-sha-256-contrato-carlos', '2029-12-31', FALSE),
    -- Maria Gomez
    ('d4d00000-0000-4000-8000-000000000003', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '9f9a6f1a-2a1d-4a61-9a1b-200000000003', 'LEGAL', 'EVIDENCIA_CONTRATO_WORM', TRUE, 'APPROVED', '8f9a6f1a-2a1d-4a61-9a1b-100000000001', '2026-05-24 09:00:00', NULL, '00000000-0000-4000-8000-000000000002', 'contrato_maria.pdf', 'hash-sha-256-contrato-maria', '2029-12-31', FALSE),
    -- Juan Perez
    ('d4d00000-0000-4000-8000-000000000004', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '9f9a6f1a-2a1d-4a61-9a1b-200000000004', 'LEGAL', 'EVIDENCIA_CONTRATO_WORM', TRUE, 'APPROVED', '8f9a6f1a-2a1d-4a61-9a1b-100000000001', '2026-05-24 09:00:00', NULL, '00000000-0000-4000-8000-000000000003', 'contrato_juan.pdf', 'hash-sha-256-contrato-juan', '2029-12-31', FALSE),
    -- Ana Rojas
    ('d4d00000-0000-4000-8000-000000000005', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '9f9a6f1a-2a1d-4a61-9a1b-200000000005', 'LEGAL', 'EVIDENCIA_CONTRATO_WORM', TRUE, 'APPROVED', '8f9a6f1a-2a1d-4a61-9a1b-100000000001', '2026-05-24 09:00:00', NULL, '00000000-0000-4000-8000-000000000004', 'contrato_ana.pdf', 'hash-sha-256-contrato-ana', '2029-12-31', FALSE)
    ON CONFLICT (doc_id) DO NOTHING;

-- Seed performance_snapshot
INSERT INTO performance_snapshot (snapshot_id, version, tenant_id, created_at, created_by, last_modified_at, last_modified_by, eval_period, score, inventory_id) VALUES
    ('d4e00000-0000-4000-8000-000000000012', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '2025-Q4', 92.5, 'd4e00000-0000-4000-8000-000000000002'),
    ('d4e00000-0000-4000-8000-000000000013', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '2025-Q4', 88.0, 'd4e00000-0000-4000-8000-000000000003'),
    ('d4e00000-0000-4000-8000-000000000014', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '2025-Q4', 95.0, 'd4e00000-0000-4000-8000-000000000004'),
    ('d4e00000-0000-4000-8000-000000000015', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '2025-Q4', 90.0, 'd4e00000-0000-4000-8000-000000000005')
    ON CONFLICT (snapshot_id) DO NOTHING;

-- Seed training_history
INSERT INTO training_history (training_id, version, tenant_id, created_at, created_by, last_modified_at, last_modified_by, course_name, doc_id, inventory_id) VALUES
    ('d4e00000-0000-4000-8000-000000000022', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', 'Clean Architecture', NULL, 'd4e00000-0000-4000-8000-000000000002'),
    ('d4e00000-0000-4000-8000-000000000023', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', 'Ley General del Trabajo', NULL, 'd4e00000-0000-4000-8000-000000000003'),
    ('d4e00000-0000-4000-8000-000000000024', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', 'Clean Architecture', NULL, 'd4e00000-0000-4000-8000-000000000004'),
    ('d4e00000-0000-4000-8000-000000000025', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', 'Ley General del Trabajo', NULL, 'd4e00000-0000-4000-8000-000000000005')
    ON CONFLICT (training_id) DO NOTHING;




--------------------------------------------------------------------------------
-- 6. Dominio Experience
--------------------------------------------------------------------------------

-- Seed experience_self_service_action (ActionType enums: DATA_UPDATE, LEAVE_REQUEST, CERTIFICATE_REQUEST)
INSERT INTO experience_self_service_action (action_id, version, tenant_id, created_at, created_by, last_modified_at, last_modified_by, person_id, action_type, payload, cert_type, cert_pdf_content, cert_sha256_hash, cert_qr_url, cert_generated_at) VALUES
    -- Carlos Mendoza: vacation request
    ('e5e00000-0000-4000-8000-000000000001', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'carlos_mendoza', '2026-05-24 09:00:00', 'SYSTEM', '8f9a6f1a-2a1d-4a61-9a1b-100000000002', 'LEAVE_REQUEST', '{"start_date": "2026-05-01", "end_date": "2026-05-05", "days": 5}', NULL, NULL, NULL, NULL, NULL),
    -- Maria Gomez: profile update
    ('e5e00000-0000-4000-8000-000000000002', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'maria_gomez', '2026-05-24 09:00:00', 'SYSTEM', '8f9a6f1a-2a1d-4a61-9a1b-100000000003', 'DATA_UPDATE', '{"bank_account": "BNB-12345678"}', NULL, NULL, NULL, NULL, NULL)
    ON CONFLICT (action_id) DO NOTHING;

-- Seed experience_approval_workflow (ApprovalStatus enums: PENDING_REVIEW, APPROVED, REJECTED, CANCELLED)
INSERT INTO experience_approval_workflow (workflow_id, version, tenant_id, created_at, created_by, last_modified_at, last_modified_by, action_id, current_step, status, history) VALUES
    -- Carlos Mendoza leaves APPROVED
    ('e5e00000-0000-4000-8000-000000000003', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', 'e5e00000-0000-4000-8000-000000000001', 2, 'APPROVED', 'Workflow initiated. Approved by supervisor.'),
    -- Maria Gomez update APPROVED
    ('e5e00000-0000-4000-8000-000000000004', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', 'e5e00000-0000-4000-8000-000000000002', 2, 'APPROVED', 'Workflow initiated. Approved by HR admin.')
    ON CONFLICT (workflow_id) DO NOTHING;

-- Seed experience_notification (requiresAcknowledgement/read_by. acknowledged_at and acknowledged_by are NOT NULL in schema)
INSERT INTO experience_notification (notif_id, version, tenant_id, created_at, created_by, last_modified_at, last_modified_by, recipient_id, channel, subject, body, sent_at, read_at, read_by, acknowledged_at, acknowledged_by) VALUES
    -- Carlos Mendoza (read_by / requiresAcknowledgement = TRUE)
    ('e5e00000-0000-4000-8000-000000000012', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '8f9a6f1a-2a1d-4a61-9a1b-100000000002', 'EMAIL', 'Vacation request approved', 'Your vacation request from May 1 to May 5 has been approved.', '2026-05-24 09:00:00', '2026-05-24 09:00:00', TRUE, '2026-05-24 09:00:00', '8f9a6f1a-2a1d-4a61-9a1b-100000000002'),
    -- Maria Gomez (read_by / requiresAcknowledgement = FALSE)
    ('e5e00000-0000-4000-8000-000000000013', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', '8f9a6f1a-2a1d-4a61-9a1b-100000000003', 'EMAIL', 'Personal info updated', 'Your personal details have been updated successfully.', '2026-05-24 09:00:00', NULL, FALSE, '2026-05-24 09:00:00', '8f9a6f1a-2a1d-4a61-9a1b-100000000003')
    ON CONFLICT (notif_id) DO NOTHING;

-- Seed experience_prediction_model (ModelType enums: CHURN, LIABILITY_RISK. ATTRITION_RISK requested maps to CHURN turnover prediction)
INSERT INTO experience_prediction_model (model_id, version, tenant_id, created_at, created_by, last_modified_at, last_modified_by, model_type, last_execution, alerts) VALUES
    ('e5e00000-0000-4000-8000-000000000005', 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00', 'SYSTEM', '2026-05-24 09:00:00', 'SYSTEM', 'CHURN', '2026-05-24 09:00:00', 'High attrition risk detected in Engineering department.')
    ON CONFLICT (model_id) DO NOTHING;


--------------------------------------------------------------------------------
-- 7. Dominio Core (Auditoría)
--------------------------------------------------------------------------------

-- Seed audit_log (representing Contract Creation and Approval, with admin_platform user ID)
INSERT INTO audit_log (action, entity, entity_id, user_id, tenant_id, occurred_at)
SELECT 'CONTRACT_CREATED', 'Contract', 'c1a00000-0000-4000-8000-000000000002', 'admin_platform', 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:00:00'
WHERE NOT EXISTS (
    SELECT 1 FROM audit_log WHERE entity_id = 'c1a00000-0000-4000-8000-000000000002' AND action = 'CONTRACT_CREATED'
);

INSERT INTO audit_log (action, entity, entity_id, user_id, tenant_id, occurred_at)
SELECT 'CONTRACT_APPROVED', 'Contract', 'c1a00000-0000-4000-8000-000000000002', 'admin_platform', 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:05:00'
WHERE NOT EXISTS (
    SELECT 1 FROM audit_log WHERE entity_id = 'c1a00000-0000-4000-8000-000000000002' AND action = 'CONTRACT_APPROVED'
);

INSERT INTO audit_log (action, entity, entity_id, user_id, tenant_id, occurred_at)
SELECT 'CONTRACT_CREATED', 'Contract', 'c1a00000-0000-4000-8000-000000000003', 'admin_platform', 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:10:00'
WHERE NOT EXISTS (
    SELECT 1 FROM audit_log WHERE entity_id = 'c1a00000-0000-4000-8000-000000000003' AND action = 'CONTRACT_CREATED'
);

INSERT INTO audit_log (action, entity, entity_id, user_id, tenant_id, occurred_at)
SELECT 'CONTRACT_APPROVED', 'Contract', 'c1a00000-0000-4000-8000-000000000003', 'admin_platform', 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:15:00'
WHERE NOT EXISTS (
    SELECT 1 FROM audit_log WHERE entity_id = 'c1a00000-0000-4000-8000-000000000003' AND action = 'CONTRACT_APPROVED'
);

INSERT INTO audit_log (action, entity, entity_id, user_id, tenant_id, occurred_at)
SELECT 'CONTRACT_APPROVED', 'Contract', 'c1a00000-0000-4000-8000-000000000004', 'admin_platform', 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', '2026-05-24 09:20:00'
WHERE NOT EXISTS (
    SELECT 1 FROM audit_log WHERE entity_id = 'c1a00000-0000-4000-8000-000000000004' AND action = 'CONTRACT_APPROVED'
);
