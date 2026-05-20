CREATE TABLE worker_profile (
  profile_id UUID PRIMARY KEY,
  relationship_id UUID NOT NULL,
  employee_no VARCHAR(255) NOT NULL,
  department VARCHAR(255),
  job_title VARCHAR(255),
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP,
  CONSTRAINT fk_worker_profile_relationship
    FOREIGN KEY (relationship_id) REFERENCES relationship (relationship_id)
);

CREATE UNIQUE INDEX idx_worker_profile_relationship_id ON worker_profile (relationship_id);
CREATE INDEX idx_worker_profile_employee_no ON worker_profile (employee_no);

