ALTER TABLE person
    ADD COLUMN email VARCHAR(255),
    ADD COLUMN phone VARCHAR(255),
    ADD COLUMN address VARCHAR(255);

UPDATE person
SET email = contact_point_email,
    phone = contact_point_phone,
    address = contact_point_address
WHERE email IS NULL
  AND phone IS NULL
  AND address IS NULL;