CREATE TABLE document_record (
  doc_id UUID PRIMARY KEY,
  relationship_id UUID NOT NULL,
  doc_category VARCHAR(255) NOT NULL,
  doc_type VARCHAR(255) NOT NULL,
  is_critical BOOLEAN NOT NULL,
  current_state VARCHAR(255) NOT NULL,
  reviewer_id UUID,
  review_date TIMESTAMP,
  reject_reason VARCHAR(255),
  storage_id UUID NOT NULL,
  file_name VARCHAR(255) NOT NULL,
  hash_sha256 VARCHAR(255) NOT NULL,
  expiry_date DATE,
  tenant_id UUID NOT NULL,
  expiration_warning_sent BOOLEAN NOT NULL
);

CREATE INDEX idx_document_record_relationship ON document_record (relationship_id);
CREATE INDEX idx_document_record_tenant ON document_record (tenant_id);
CREATE INDEX idx_document_record_category ON document_record (doc_category);

