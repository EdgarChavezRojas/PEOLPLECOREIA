-- Flyway Migration: V20260524170800__seed_iam_users
-- Description: Seed initial modules, actions, resources, roles, users, and permissions with credible dev data

-- 1. Seed iam_module
INSERT INTO iam_module (id, version, tenant_id, created_at, created_by, last_modified_at, last_modified_by, code, name) VALUES
(1, 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', CURRENT_TIMESTAMP, 'SYSTEM', CURRENT_TIMESTAMP, 'SYSTEM', 'CORE', 'Core System Module');

-- 2. Seed iam_action
INSERT INTO iam_action (id, version, tenant_id, created_at, created_by, last_modified_at, last_modified_by, code, name) VALUES
(1, 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', CURRENT_TIMESTAMP, 'SYSTEM', CURRENT_TIMESTAMP, 'SYSTEM', 'READ', 'Read Access'),
(2, 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', CURRENT_TIMESTAMP, 'SYSTEM', CURRENT_TIMESTAMP, 'SYSTEM', 'WRITE', 'Write Access'),
(3, 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', CURRENT_TIMESTAMP, 'SYSTEM', CURRENT_TIMESTAMP, 'SYSTEM', 'DELETE', 'Delete Access');

-- 3. Seed iam_resource
INSERT INTO iam_resource (id, version, tenant_id, created_at, created_by, last_modified_at, last_modified_by, code, name, module_id) VALUES
(1, 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', CURRENT_TIMESTAMP, 'SYSTEM', CURRENT_TIMESTAMP, 'SYSTEM', 'USER', 'User Management', 1),
(2, 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', CURRENT_TIMESTAMP, 'SYSTEM', CURRENT_TIMESTAMP, 'SYSTEM', 'CONTRACT', 'Contract Management', 1),
(3, 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', CURRENT_TIMESTAMP, 'SYSTEM', CURRENT_TIMESTAMP, 'SYSTEM', 'ATTENDANCE', 'Attendance Management', 1);

-- 4. Seed iam_role
INSERT INTO iam_role (id, version, tenant_id, created_at, created_by, last_modified_at, last_modified_by, name, description) VALUES
(1, 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', CURRENT_TIMESTAMP, 'SYSTEM', CURRENT_TIMESTAMP, 'SYSTEM', 'ADMIN', 'Administrator with full privileges'),
(2, 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', CURRENT_TIMESTAMP, 'SYSTEM', CURRENT_TIMESTAMP, 'SYSTEM', 'EMPLOYEE', 'Employee with standard privileges');

-- 5. Seed iam_user
-- Note: Simple clear-text passwords as requested (admin123 and user123)
INSERT INTO iam_user (id, version, tenant_id, created_at, created_by, last_modified_at, last_modified_by, username, email, password, active) VALUES
(1, 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', CURRENT_TIMESTAMP, 'SYSTEM', CURRENT_TIMESTAMP, 'SYSTEM', 'admin_platform', 'admin.platform@solveria.com', 'admin123', TRUE),
(2, 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', CURRENT_TIMESTAMP, 'SYSTEM', CURRENT_TIMESTAMP, 'SYSTEM', 'carlos_mendoza', 'carlos.mendoza@solveria.com', 'user123', TRUE),
(3, 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', CURRENT_TIMESTAMP, 'SYSTEM', CURRENT_TIMESTAMP, 'SYSTEM', 'maria_gomez', 'maria.gomez@solveria.com', 'user1234', TRUE);

-- 6. Seed iam_user_roles (ManyToMany association)
INSERT INTO iam_user_roles (user_id, role_id) VALUES
(1, 1), -- admin_platform has ADMIN role
(2, 2), -- carlos_mendoza has EMPLOYEE role
(3, 2); -- maria_gomez has EMPLOYEE role

-- 7. Seed iam_permission (Grant permissions to roles)
INSERT INTO iam_permission (id, version, tenant_id, created_at, created_by, last_modified_at, last_modified_by, role_id, module_id, resource_id, action_id, field_id) VALUES
(1, 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', CURRENT_TIMESTAMP, 'SYSTEM', CURRENT_TIMESTAMP, 'SYSTEM', 1, 1, 1, 1, NULL), -- ADMIN can READ USER
(2, 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', CURRENT_TIMESTAMP, 'SYSTEM', CURRENT_TIMESTAMP, 'SYSTEM', 1, 1, 1, 2, NULL), -- ADMIN can WRITE USER
(3, 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', CURRENT_TIMESTAMP, 'SYSTEM', CURRENT_TIMESTAMP, 'SYSTEM', 1, 1, 1, 3, NULL), -- ADMIN can DELETE USER
(4, 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', CURRENT_TIMESTAMP, 'SYSTEM', CURRENT_TIMESTAMP, 'SYSTEM', 1, 1, 2, 1, NULL), -- ADMIN can READ CONTRACT
(5, 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', CURRENT_TIMESTAMP, 'SYSTEM', CURRENT_TIMESTAMP, 'SYSTEM', 1, 1, 2, 2, NULL), -- ADMIN can WRITE CONTRACT
(6, 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', CURRENT_TIMESTAMP, 'SYSTEM', CURRENT_TIMESTAMP, 'SYSTEM', 1, 1, 3, 1, NULL), -- ADMIN can READ ATTENDANCE
(7, 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', CURRENT_TIMESTAMP, 'SYSTEM', CURRENT_TIMESTAMP, 'SYSTEM', 1, 1, 3, 2, NULL), -- ADMIN can WRITE ATTENDANCE
(8, 0, 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b', CURRENT_TIMESTAMP, 'SYSTEM', CURRENT_TIMESTAMP, 'SYSTEM', 2, 1, 3, 1, NULL); -- EMPLOYEE can READ ATTENDANCE
