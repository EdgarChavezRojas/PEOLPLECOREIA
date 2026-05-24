-- Migración para poblar la base de datos con roles y usuarios iniciales

-- 1. ADVERTENCIA: En el script original V202605191204__init_iam_user.sql no existía la columna password.
-- Como implementamos login con credenciales, la agregamos aquí de forma segura si no existe.
ALTER TABLE iam_user ADD COLUMN IF NOT EXISTS password VARCHAR(255);

-- 2. Insertar Roles iniciales (Se fuerzan los IDs 1 y 2 para vincularlos fácilmente)
INSERT INTO iam_role (id, version, tenant_id, created_at, created_by, name, description)
VALUES
    (1, 1, '123e4567-e89b-12d3-a456-426614174000', CURRENT_TIMESTAMP, 'system', 'ADMIN', 'Rol de Administrador'),
    (2, 1, '123e4567-e89b-12d3-a456-426614174000', CURRENT_TIMESTAMP, 'system', 'USER', 'Rol de Empleado Regular');

-- Ajustar la secuencia autoincremental de PostgreSQL para iam_role
SELECT setval(pg_get_serial_sequence('iam_role', 'id'), coalesce(max(id),0) + 1, false) FROM iam_role;

-- 3. Insertar Usuarios
-- NOTA: Todos tienen la contraseña "password"
-- El hash BCrypt para "password" es: $2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HCGKKPTOsEp/nAVE4sqso
INSERT INTO iam_user (id, version, tenant_id, created_at, created_by, username, email, active, password)
VALUES
    (1, 1, '123e4567-e89b-12d3-a456-426614174000', CURRENT_TIMESTAMP, 'system', 'admin_solveria', 'admin@solveria.com', true, '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HCGKKPTOsEp/nAVE4sqso'),
    (2, 1, '123e4567-e89b-12d3-a456-426614174000', CURRENT_TIMESTAMP, 'system', 'juan_perez', 'juan.perez@solveria.com', true, '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HCGKKPTOsEp/nAVE4sqso'),
    (3, 1, '123e4567-e89b-12d3-a456-426614174000', CURRENT_TIMESTAMP, 'system', 'maria_gomez', 'maria.gomez@solveria.com', true, '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HCGKKPTOsEp/nAVE4sqso');

-- Ajustar la secuencia autoincremental de PostgreSQL para iam_user
SELECT setval(pg_get_serial_sequence('iam_user', 'id'), coalesce(max(id),0) + 1, false) FROM iam_user;

-- 4. Asignar Roles a Usuarios (Tabla intermedia iam_user_roles)
INSERT INTO iam_user_roles (user_id, role_id)
VALUES
    (1, 1), -- admin_solveria -> ADMIN
    (2, 2), -- juan_perez -> USER
    (3, 2); -- maria_gomez -> USER
