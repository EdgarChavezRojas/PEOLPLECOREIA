-- Flyway Migration: V20260524170000__init_scheduling_domain
-- Description: Initialize Scheduling tables (SchedulePlan, AssignedShift, AttendanceRecord, TimeEntry)

-- 1. sch_schedule_plan Table
CREATE TABLE sch_schedule_plan (
  plan_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  unit_id UUID NOT NULL,
  period_start DATE NOT NULL,
  period_end DATE NOT NULL,
  status VARCHAR(255) NOT NULL,
  total_projected_cost NUMERIC(15, 2),
  CONSTRAINT fk_sch_plan_unit FOREIGN KEY (unit_id) REFERENCES org_unit (unit_id)
);

-- 2. sch_assigned_shift Table
CREATE TABLE sch_assigned_shift (
  shift_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  plan_id UUID NOT NULL,
  relationship_id UUID NOT NULL,
  expected_start TIMESTAMP NOT NULL,
  expected_end TIMESTAMP NOT NULL,
  shift_type VARCHAR(255) NOT NULL,
  is_active BOOLEAN NOT NULL,
  metadata JSONB,
  violations JSONB,
  CONSTRAINT fk_sch_shift_plan FOREIGN KEY (plan_id) REFERENCES sch_schedule_plan (plan_id),
  CONSTRAINT fk_sch_shift_relationship FOREIGN KEY (relationship_id) REFERENCES relationship (relationship_id)
);

-- 3. sch_attendance_record Table
CREATE TABLE sch_attendance_record (
  record_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  relationship_id UUID NOT NULL,
  work_date DATE NOT NULL,
  is_closed BOOLEAN NOT NULL,
  status VARCHAR(255) NOT NULL,
  deviations JSONB,
  CONSTRAINT fk_sch_attendance_relationship FOREIGN KEY (relationship_id) REFERENCES relationship (relationship_id)
);

-- 4. sch_time_entry Table
CREATE TABLE sch_time_entry (
  entry_id UUID PRIMARY KEY,
  version BIGINT,
  tenant_id UUID NOT NULL,
  created_at TIMESTAMP NOT NULL,
  created_by VARCHAR(255),
  last_modified_at TIMESTAMP,
  last_modified_by VARCHAR(255),
  record_id UUID NOT NULL,
  punch_time TIMESTAMP NOT NULL,
  punch_type VARCHAR(255) NOT NULL,
  device_id VARCHAR(255),
  source VARCHAR(255),
  geo_validation JSONB,
  CONSTRAINT fk_sch_entry_record FOREIGN KEY (record_id) REFERENCES sch_attendance_record (record_id)
);
