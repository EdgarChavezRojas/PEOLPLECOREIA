-- Flyway Migration: V20260602190000__sync_iam_sequences
-- Description: Synchronize PostgreSQL sequences for IAM tables after seeding data with manual IDs

SELECT setval(pg_get_serial_sequence('iam_module', 'id'), COALESCE(MAX(id), 1)) FROM iam_module;
SELECT setval(pg_get_serial_sequence('iam_action', 'id'), COALESCE(MAX(id), 1)) FROM iam_action;
SELECT setval(pg_get_serial_sequence('iam_resource', 'id'), COALESCE(MAX(id), 1)) FROM iam_resource;
SELECT setval(pg_get_serial_sequence('iam_role', 'id'), COALESCE(MAX(id), 1)) FROM iam_role;
SELECT setval(pg_get_serial_sequence('iam_user', 'id'), COALESCE(MAX(id), 1)) FROM iam_user;
SELECT setval(pg_get_serial_sequence('iam_permission', 'id'), COALESCE(MAX(id), 1)) FROM iam_permission;
