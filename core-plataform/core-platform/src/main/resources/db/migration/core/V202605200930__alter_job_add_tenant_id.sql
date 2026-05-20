ALTER TABLE job
    ADD COLUMN tenant_id UUID;

-- Opcional si necesitas poblar valores existentes antes de poner NOT NULL
-- UPDATE job SET tenant_id = '<UUID_TENANT_POR_DEFECTO>' WHERE tenant_id IS NULL;

ALTER TABLE job
    ALTER COLUMN tenant_id SET NOT NULL;