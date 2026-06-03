-- Flyway Migration: V20260601220000__alter_notification_make_acknowledged_nullable
-- Description: Make acknowledged_at and acknowledged_by columns nullable in experience_notification

ALTER TABLE experience_notification ALTER COLUMN acknowledged_at DROP NOT NULL;
ALTER TABLE experience_notification ALTER COLUMN acknowledged_by DROP NOT NULL;
