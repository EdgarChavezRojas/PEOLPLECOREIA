package com.solveria.payroll.application.port.outbound;

import com.solveria.payroll.domain.model.event.PayrollPeriodClosedEvent;

public interface EventOutboxPort {
  void publish(PayrollPeriodClosedEvent event);
}
