CREATE TABLE experience_notification (
  notif_id UUID PRIMARY KEY,
  recipient_id UUID NOT NULL,
  channel VARCHAR(20) NOT NULL,
  subject VARCHAR(255) NOT NULL,
  body TEXT,
  tenant_id UUID NOT NULL,
  sent_at TIMESTAMPTZ NOT NULL,
  read_at TIMESTAMPTZ,
  read_by BOOLEAN NOT NULL,
  acknowledged_at TIMESTAMPTZ NOT NULL,
  acknowledged_by UUID NOT NULL
);

