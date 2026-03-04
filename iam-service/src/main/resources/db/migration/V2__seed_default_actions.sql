-- Flyway Migration V2: Seed Default Actions
-- Author: Phase B Remediation (2026-02-23)
-- Description: Inserts default CRUD actions for IAM system
-- Updated: id is now BIGINT GENERATED ALWAYS AS IDENTITY — no explicit id insertion needed.

INSERT INTO iam_action (name, description, tenant_id, created_at, version)
VALUES ('CREATE', 'Create new entities', 'system', CURRENT_TIMESTAMP, 0)
ON CONFLICT (name) DO NOTHING;

INSERT INTO iam_action (name, description, tenant_id, created_at, version)
VALUES ('READ', 'Read/view entities', 'system', CURRENT_TIMESTAMP, 0)
ON CONFLICT (name) DO NOTHING;

INSERT INTO iam_action (name, description, tenant_id, created_at, version)
VALUES ('UPDATE', 'Update existing entities', 'system', CURRENT_TIMESTAMP, 0)
ON CONFLICT (name) DO NOTHING;

INSERT INTO iam_action (name, description, tenant_id, created_at, version)
VALUES ('DELETE', 'Delete entities', 'system', CURRENT_TIMESTAMP, 0)
ON CONFLICT (name) DO NOTHING;

INSERT INTO iam_action (name, description, tenant_id, created_at, version)
VALUES ('EXECUTE', 'Execute operations/actions', 'system', CURRENT_TIMESTAMP, 0)
ON CONFLICT (name) DO NOTHING;
