-- Flyway Migration: V20260602191000__create_position_occupant_table
-- Description: Create position_occupant table to map multiple person assignments to a position

CREATE TABLE position_occupant (
  position_id UUID NOT NULL,
  person_id UUID NOT NULL,
  PRIMARY KEY (position_id, person_id),
  CONSTRAINT fk_position_occupant_position FOREIGN KEY (position_id) REFERENCES position (position_id) ON DELETE CASCADE,
  CONSTRAINT fk_position_occupant_person FOREIGN KEY (person_id) REFERENCES person (person_id) ON DELETE CASCADE
);

CREATE INDEX idx_position_occupant_position_id ON position_occupant (position_id);
CREATE INDEX idx_position_occupant_person_id ON position_occupant (person_id);
