CREATE TABLE leave_transaction (
  transaction_id UUID PRIMARY KEY,
  balance_id UUID NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  days_requested NUMERIC NOT NULL,
  status VARCHAR(255) NOT NULL,
  CONSTRAINT fk_leave_transaction_balance
    FOREIGN KEY (balance_id) REFERENCES accrual_balance (balance_id)
);

