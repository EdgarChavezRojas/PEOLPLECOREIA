CREATE TABLE experience_approval_workflow (
  workflow_id UUID PRIMARY KEY,
  action_id UUID NOT NULL,
  current_step INTEGER NOT NULL,
  status VARCHAR(20) NOT NULL,
  history TEXT,
  created_at TIMESTAMPTZ NOT NULL,
  CONSTRAINT fk_experience_approval_workflow_action
    FOREIGN KEY (action_id) REFERENCES experience_self_service_action (action_id)
);

CREATE UNIQUE INDEX uq_experience_approval_workflow_action_id
  ON experience_approval_workflow (action_id);

