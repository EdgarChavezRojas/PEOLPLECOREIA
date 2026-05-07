-- ============================================================================
-- BC 6: Interaction & Intelligent Experience - Database Schema
-- PeopleCoreIA Platform - Santa Cruz, Bolivia 2026
-- ============================================================================

-- ─── SelfServiceAction (AR) ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS experience_self_service_action (
    action_id       UUID            NOT NULL PRIMARY KEY,
    person_id       UUID            NOT NULL,
    action_type     VARCHAR(30)     NOT NULL,     -- DATA_UPDATE, LEAVE_REQUEST, CERTIFICATE_REQUEST
    payload         TEXT,                          -- JSON datos temporales pre-impacto
    tenant_id       VARCHAR(50)     NOT NULL,
    created_by      VARCHAR(100)    NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Certificate payload fields (W14)
    cert_type           VARCHAR(50),
    cert_pdf_content    TEXT,
    cert_sha256_hash    VARCHAR(64),              -- SHA-256 hash para integridad
    cert_qr_url         VARCHAR(512),             -- Zero-Trust QR validation URL
    cert_generated_at   TIMESTAMP
);

CREATE INDEX idx_ssa_person_tenant ON experience_self_service_action(person_id, tenant_id);
CREATE INDEX idx_ssa_tenant ON experience_self_service_action(tenant_id);

-- ─── ApprovalWorkflow (Entity) ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS experience_approval_workflow (
    workflow_id     UUID            NOT NULL PRIMARY KEY,
    action_id       UUID            NOT NULL UNIQUE,
    current_step    INT             NOT NULL DEFAULT 1,
    status          VARCHAR(20)     NOT NULL,     -- PENDING_REVIEW, APPROVED, REJECTED, CANCELLED
    history         TEXT,                          -- JSON: Invariante SoD (Quién/Cuándo)
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_aw_action FOREIGN KEY (action_id)
        REFERENCES experience_self_service_action(action_id) ON DELETE CASCADE
);

-- ─── Notification (Entity) ─────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS experience_notification (
    notif_id        UUID            NOT NULL PRIMARY KEY,
    recipient_id    UUID            NOT NULL,
    channel         VARCHAR(20)     NOT NULL,     -- PUSH_MOBILE, EMAIL
    subject         VARCHAR(255)    NOT NULL,
    body            TEXT,
    tenant_id       VARCHAR(50)     NOT NULL,
    sent_at         TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at         TIMESTAMP                     -- Auditoría de notificación legal
);

CREATE INDEX idx_notif_recipient_tenant ON experience_notification(recipient_id, tenant_id);

-- ─── PredictionModel (AR) ──────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS experience_prediction_model (
    model_id        UUID            NOT NULL PRIMARY KEY,
    model_type      VARCHAR(30)     NOT NULL,     -- CHURN, LIABILITY_RISK
    version         VARCHAR(20)     NOT NULL,
    last_execution  TIMESTAMP,
    tenant_id       VARCHAR(50)     NOT NULL,
    alerts          TEXT                           -- JSON array de RiskAlert
);

CREATE INDEX idx_pm_tenant ON experience_prediction_model(tenant_id);

-- ─── Event Outbox (Transactional Outbox Pattern) ───────────────────────────
CREATE TABLE IF NOT EXISTS experience_event_outbox (
    event_id        UUID            NOT NULL PRIMARY KEY,
    aggregate_type  VARCHAR(50)     NOT NULL,
    aggregate_id    UUID            NOT NULL,
    event_type      VARCHAR(100)    NOT NULL,
    payload         TEXT            NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',  -- PENDING, PROCESSED, FAILED
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_eeo_status ON experience_event_outbox(status);
CREATE INDEX idx_eeo_aggregate ON experience_event_outbox(aggregate_type, aggregate_id);
