-- Flyway Migration: V20260524170300__init_time_and_bearing_domain
-- Description: Initialize Time & Bearings tables (ClockingDevice, BiometricEnrollment, AuditLog, PunchAttemptLog, TimesheetPeriod, DailyConsolidation, PayrollHandoff, AttendanceLedger, TimeEntry, TimeDeviation)

-- 1. clocking_device Table
CREATE TABLE clocking_device (
  device_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  org_unit_id UUID NOT NULL,
  serial_number VARCHAR(100) NOT NULL,
  device_type VARCHAR(30) NOT NULL,
  device_role VARCHAR(20) NOT NULL,
  status VARCHAR(20) NOT NULL,
  installed_at TIMESTAMP,
  decommissioned_at TIMESTAMP,
  supports_fingerprint BOOLEAN,
  supports_facial BOOLEAN,
  supports_nfc BOOLEAN,
  supports_qr BOOLEAN,
  firmware_version VARCHAR(50),
  public_key_pem TEXT,
  last_seen_at TIMESTAMP,
  battery_level INTEGER,
  sync_status VARCHAR(20),
  enrolled_employees_count INTEGER,
  CONSTRAINT uk_device_serial_tenant UNIQUE (serial_number, tenant_id),
  CONSTRAINT fk_clocking_device_unit FOREIGN KEY (org_unit_id) REFERENCES org_unit (unit_id)
);

-- 2. biometric_enrollment Table
CREATE TABLE biometric_enrollment (
  enrollment_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  device_id UUID NOT NULL,
  relationship_id UUID NOT NULL,
  biometric_type VARCHAR(20) NOT NULL,
  template_hash VARCHAR(128) NOT NULL,
  template_quality_score NUMERIC(4, 2) NOT NULL,
  status VARCHAR(20) NOT NULL,
  enrolled_at TIMESTAMP NOT NULL,
  revoked_at TIMESTAMP,
  revocation_reason VARCHAR(30),
  CONSTRAINT uk_enrollment_relationship_type_active UNIQUE (relationship_id, biometric_type, status, tenant_id),
  CONSTRAINT fk_biometric_enrollment_device FOREIGN KEY (device_id) REFERENCES clocking_device (device_id),
  CONSTRAINT fk_biometric_enrollment_relationship FOREIGN KEY (relationship_id) REFERENCES relationship (relationship_id)
);

-- 3. device_audit_log Table
CREATE TABLE device_audit_log (
  audit_log_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  device_id UUID NOT NULL,
  event_type VARCHAR(50) NOT NULL,
  actor_id VARCHAR(100),
  occurred_at TIMESTAMP NOT NULL,
  description TEXT,
  CONSTRAINT fk_device_audit_device FOREIGN KEY (device_id) REFERENCES clocking_device (device_id)
);

-- 4. punch_attempt_log Table
CREATE TABLE punch_attempt_log (
  attempt_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  device_id UUID NOT NULL,
  attempted_at TIMESTAMP NOT NULL,
  relationship_id UUID NOT NULL,
  auth_method VARCHAR(20) NOT NULL,
  auth_result VARCHAR(30) NOT NULL,
  security_incident BOOLEAN NOT NULL,
  incident_escalated_to UUID,
  CONSTRAINT fk_punch_attempt_device FOREIGN KEY (device_id) REFERENCES clocking_device (device_id),
  CONSTRAINT fk_punch_attempt_relationship FOREIGN KEY (relationship_id) REFERENCES relationship (relationship_id)
);

-- 5. timesheet_period Table
CREATE TABLE timesheet_period (
  period_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  org_unit_id UUID NOT NULL,
  period_type VARCHAR(20) NOT NULL,
  period_start DATE NOT NULL,
  period_end DATE NOT NULL,
  grace_period_end TIMESTAMP NOT NULL,
  status VARCHAR(20) NOT NULL,
  closed_at TIMESTAMP,
  closed_by UUID,
  closure_type VARCHAR(10),
  payroll_event_emitted_at TIMESTAMP,
  CONSTRAINT fk_timesheet_period_unit FOREIGN KEY (org_unit_id) REFERENCES org_unit (unit_id)
);

-- 6. daily_consolidation_summary Table
CREATE TABLE daily_consolidation_summary (
  summary_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  period_id UUID NOT NULL,
  work_date DATE NOT NULL,
  total_scheduled INTEGER NOT NULL,
  total_attended INTEGER NOT NULL,
  total_no_shows INTEGER NOT NULL,
  total_exceptions_pending INTEGER NOT NULL,
  total_regular_hours NUMERIC(8, 2) NOT NULL,
  total_overtime_hours NUMERIC(8, 2) NOT NULL,
  total_night_hours NUMERIC(8, 2) NOT NULL,
  calculated_at TIMESTAMP NOT NULL,
  CONSTRAINT fk_daily_summary_period FOREIGN KEY (period_id) REFERENCES timesheet_period (period_id)
);

-- 7. payroll_handoff_package Table
CREATE TABLE payroll_handoff_package (
  handoff_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  period_id UUID NOT NULL,
  generated_at TIMESTAMP NOT NULL,
  checksum VARCHAR(128) NOT NULL,
  CONSTRAINT uq_payroll_handoff_period UNIQUE (period_id),
  CONSTRAINT fk_payroll_handoff_period FOREIGN KEY (period_id) REFERENCES timesheet_period (period_id)
);

-- 8. payroll_handoff_employee_record Table (Element Collection)
CREATE TABLE payroll_handoff_employee_record (
  handoff_id UUID NOT NULL,
  relationship_id UUID NOT NULL,
  regular_hours_total NUMERIC(8, 2) NOT NULL,
  overtime_hours_total NUMERIC(8, 2) NOT NULL,
  night_hours_total NUMERIC(8, 2) NOT NULL,
  holiday_hours_total NUMERIC(8, 2) NOT NULL,
  unjustified_absences INTEGER NOT NULL,
  remote_work_days INTEGER NOT NULL,
  data_quality_flag VARCHAR(30) NOT NULL,
  PRIMARY KEY (handoff_id, relationship_id),
  CONSTRAINT fk_handoff_employee_record FOREIGN KEY (handoff_id) REFERENCES payroll_handoff_package (handoff_id),
  CONSTRAINT fk_handoff_employee_relationship FOREIGN KEY (relationship_id) REFERENCES relationship (relationship_id)
);

-- 9. attendance_ledger Table
CREATE TABLE attendance_ledger (
  ledger_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  relationship_id UUID NOT NULL,
  org_unit_id UUID NOT NULL,
  work_date DATE NOT NULL,
  shift_id UUID,
  status VARCHAR(30) NOT NULL,
  is_finalized BOOLEAN NOT NULL,
  remote_work BOOLEAN NOT NULL,
  remote_work_auth_id UUID,
  closed_at TIMESTAMP,
  regular_hours NUMERIC(6, 2),
  overtime_hours NUMERIC(6, 2),
  night_hours NUMERIC(6, 2),
  holiday_hours NUMERIC(6, 2),
  deducted_break_minutes INTEGER,
  net_payable_hours NUMERIC(6, 2),
  summary_calculated_at TIMESTAMP,
  CONSTRAINT uk_ledger_relationship_date UNIQUE (relationship_id, work_date, tenant_id),
  CONSTRAINT fk_attendance_ledger_relationship FOREIGN KEY (relationship_id) REFERENCES relationship (relationship_id),
  CONSTRAINT fk_attendance_ledger_unit FOREIGN KEY (org_unit_id) REFERENCES org_unit (unit_id)
);

-- 10. time_entry Table
CREATE TABLE time_entry (
  entry_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  ledger_id UUID NOT NULL,
  punch_time TIMESTAMP NOT NULL,
  punch_type VARCHAR(30) NOT NULL,
  source VARCHAR(30) NOT NULL,
  device_id UUID,
  device_signature TEXT,
  ip_address VARCHAR(45),
  user_agent TEXT,
  latitude NUMERIC(9, 6),
  longitude NUMERIC(9, 6),
  accuracy_meters NUMERIC(8, 2),
  org_extension_snapshot VARCHAR(3),
  is_within_extension BOOLEAN,
  geo_status VARCHAR(30),
  is_retroactive BOOLEAN NOT NULL,
  retroactive_approver_id UUID,
  corrects_entry_id UUID,
  fraud_flag BOOLEAN NOT NULL,
  CONSTRAINT fk_time_entry_ledger FOREIGN KEY (ledger_id) REFERENCES attendance_ledger (ledger_id)
);

-- 11. time_deviation_record Table
CREATE TABLE time_deviation_record (
  deviation_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  ledger_id UUID NOT NULL,
  deviation_type VARCHAR(40) NOT NULL,
  deviation_minutes INTEGER NOT NULL,
  resolution_status VARCHAR(40) NOT NULL,
  detected_at TIMESTAMP NOT NULL,
  resolved_at TIMESTAMP,
  resolved_by UUID,
  reason_note TEXT,
  secondary_approver_id UUID,
  CONSTRAINT fk_time_deviation_ledger FOREIGN KEY (ledger_id) REFERENCES attendance_ledger (ledger_id)
);
