CREATE TABLE person (
  person_id UUID PRIMARY KEY,
  first_name VARCHAR(255) NOT NULL,
  last_name VARCHAR(255) NOT NULL,
  birth_date DATE NOT NULL,
  gender VARCHAR(255),
  marital_status VARCHAR(12),
  profession_title VARCHAR(255),
  global_id VARCHAR(255) NOT NULL,
  contact_point_email VARCHAR(255),
  contact_point_phone VARCHAR(255),
  contact_point_address VARCHAR(255),
  created_at DATE NOT NULL,
  updated_at DATE
);

CREATE UNIQUE INDEX idx_person_global_id ON person (global_id);

