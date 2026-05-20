CREATE TABLE experience_prediction_model (
  model_id UUID PRIMARY KEY,
  model_type VARCHAR(30) NOT NULL,
  version VARCHAR(20) NOT NULL,
  last_execution TIMESTAMPTZ,
  tenant_id UUID NOT NULL,
  alerts TEXT
);

