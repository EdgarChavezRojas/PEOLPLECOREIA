CREATE TABLE experience_self_service_action (
  action_id UUID PRIMARY KEY,
  person_id UUID NOT NULL,
  action_type VARCHAR(30) NOT NULL,
  payload TEXT,
  tenant_id UUID NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL,
  cert_type VARCHAR(50),
  cert_pdf_content TEXT,
  cert_sha256_hash VARCHAR(64),
  cert_qr_url VARCHAR(512),
  cert_generated_at TIMESTAMPTZ
);

