CREATE TABLE status_log (
  log_id UUID PRIMARY KEY,
  relationship_id UUID NOT NULL,
  previous_status VARCHAR(255),
  new_status VARCHAR(255) NOT NULL,
  change_reason VARCHAR(255),
  changed_at DATE NOT NULL,
  changed_by UUID,
  CONSTRAINT fk_status_log_relationship
    FOREIGN KEY (relationship_id) REFERENCES relationship (relationship_id)
);

CREATE INDEX idx_status_log_relationship_id ON status_log (relationship_id);
CREATE INDEX idx_status_log_changed_at ON status_log (changed_at);

