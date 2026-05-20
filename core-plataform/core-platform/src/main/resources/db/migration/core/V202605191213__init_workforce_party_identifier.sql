CREATE TABLE party_identifier (
  identifier_id UUID PRIMARY KEY,
  person_id UUID NOT NULL,
  id_type VARCHAR(255) NOT NULL,
  id_number VARCHAR(255) NOT NULL,
  extension VARCHAR(255),
  issue_date DATE,
  expiry_date DATE,
  created_at DATE NOT NULL,
  CONSTRAINT fk_party_identifier_person
    FOREIGN KEY (person_id) REFERENCES person (person_id)
);

CREATE INDEX idx_identifier_person_id ON party_identifier (person_id);
CREATE UNIQUE INDEX idx_identifier_id_number ON party_identifier (id_number);
CREATE INDEX idx_identifier_expiry ON party_identifier (expiry_date);

