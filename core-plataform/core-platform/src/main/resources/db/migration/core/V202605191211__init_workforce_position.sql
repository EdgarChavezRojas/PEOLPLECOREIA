CREATE TABLE position (
  position_id UUID PRIMARY KEY,
  unit_id UUID NOT NULL,
  job_id UUID NOT NULL,
  pos_status VARCHAR(255) NOT NULL,
  is_budgeted BOOLEAN,
  headcount_plan_max_slots INTEGER,
  headcount_plan_current_slots INTEGER,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP,
  CONSTRAINT fk_position_job
    FOREIGN KEY (job_id) REFERENCES job (job_id)
);

CREATE INDEX idx_position_unit_id ON position (unit_id);
CREATE INDEX idx_position_status ON position (pos_status);

