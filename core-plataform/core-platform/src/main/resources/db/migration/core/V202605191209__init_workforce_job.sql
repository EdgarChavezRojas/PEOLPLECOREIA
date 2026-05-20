CREATE TABLE job (
  job_id UUID PRIMARY KEY,
  job_code VARCHAR(255) NOT NULL,
  title VARCHAR(255) NOT NULL,
  grade_band VARCHAR(255),
  description VARCHAR(255),
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP
);

CREATE UNIQUE INDEX idx_job_code ON job (job_code);

