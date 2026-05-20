CREATE TABLE holiday_calendar (
  holiday_id UUID PRIMARY KEY,
  holiday_date DATE NOT NULL,
  scope VARCHAR(255) NOT NULL,
  tenant_id UUID NOT NULL
);

CREATE INDEX idx_holiday_calendar_date ON holiday_calendar (holiday_date);
CREATE INDEX idx_holiday_calendar_tenant ON holiday_calendar (tenant_id);

