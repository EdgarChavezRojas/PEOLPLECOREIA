CREATE TABLE shared_event_outbox (
                                     event_id UUID PRIMARY KEY,
                                     aggregate_type VARCHAR(50),
                                     aggregate_id UUID,
                                     type VARCHAR(100) NOT NULL,
                                     payload TEXT NOT NULL,
                                     state VARCHAR(20) NOT NULL,
                                     created_at TIMESTAMP NOT NULL,
                                     processed_at TIMESTAMP,
                                     error_log TEXT
);

CREATE INDEX idx_shared_event_outbox_state ON shared_event_outbox (state);
CREATE INDEX idx_shared_event_outbox_created_at ON shared_event_outbox (created_at);