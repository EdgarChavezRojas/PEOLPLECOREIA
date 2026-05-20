CREATE TABLE academic_profile (
  academic_id UUID PRIMARY KEY,
  relationship_id UUID NOT NULL,
  current_rank VARCHAR(255),
  teaching_load INTEGER,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP,
  CONSTRAINT fk_academic_profile_relationship
    FOREIGN KEY (relationship_id) REFERENCES relationship (relationship_id)
);

CREATE UNIQUE INDEX idx_academic_profile_relationship_id ON academic_profile (relationship_id);
CREATE INDEX idx_academic_profile_rank ON academic_profile (current_rank);

