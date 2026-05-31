-- Flyway Migration: V20260530180000__update_payroll_approval_user_refs
-- Description: Drop creator_ref and use BIGINT for reviewer_ref/approver_ref.

ALTER TABLE prl_payroll_approval
  DROP COLUMN IF EXISTS creator_ref;

ALTER TABLE prl_payroll_approval
  DROP COLUMN IF EXISTS reviewer_ref,
  DROP COLUMN IF EXISTS approver_ref;

ALTER TABLE prl_payroll_approval
  ADD COLUMN reviewer_ref BIGINT,
  ADD COLUMN approver_ref BIGINT;

