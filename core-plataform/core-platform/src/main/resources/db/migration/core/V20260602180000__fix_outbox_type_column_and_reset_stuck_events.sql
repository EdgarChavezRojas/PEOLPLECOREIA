-- =============================================================================
-- FIX: Ampliar columna type de 100 a 255 para FQCN largos
-- FIX: Resetear eventos FAILED atascados por el bug del loop del OutboxRelay
-- =============================================================================

-- 1. Ampliar la columna type para soportar FQCNs completos
ALTER TABLE shared_event_outbox
    ALTER COLUMN type TYPE VARCHAR(255);

-- 2. Agregar columna retry_count para rastrear reintentos del relay
ALTER TABLE shared_event_outbox
    ADD COLUMN IF NOT EXISTS retry_count INTEGER NOT NULL DEFAULT 0;

-- 3. Marcar todos los eventos FAILED como PROCESSED
--    Estos eventos ya fueron consumidos por los @EventListener de Spring
--    durante la transacción original. El FAILED era causado por el bug del
--    OutboxRelay que intentaba re-deserializar y re-publicar los eventos.
UPDATE shared_event_outbox
SET state       = 'PROCESSED',
    error_log   = 'AUTO_FIXED: Reset by migration V20260602180000 - events were already processed by Spring @EventListener',
    processed_at = NOW()
WHERE state = 'FAILED';

-- 4. Marcar todos los eventos PENDING como PROCESSED
--    Misma razón: ya fueron consumidos sincrónicamente.
UPDATE shared_event_outbox
SET state       = 'PROCESSED',
    error_log   = 'AUTO_FIXED: Reset by migration V20260602180000 - events were already processed by Spring @EventListener',
    processed_at = NOW()
WHERE state = 'PENDING';
