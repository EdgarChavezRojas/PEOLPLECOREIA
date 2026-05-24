package com.solveria.payroll.application.usecase;

import com.solveria.core.shared.exceptions.SolverExceptionImpl;
import com.solveria.payroll.application.port.inbound.ClosePayrollUseCase;
import com.solveria.payroll.application.port.outbound.EventOutboxPort;
import com.solveria.payroll.application.port.outbound.PayrollClosureRepositoryPort;
import com.solveria.payroll.domain.model.entity.PayrollClosure;
import com.solveria.payroll.domain.model.event.PayrollPeriodClosedEvent;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProcessPayrollClosureUseCase implements ClosePayrollUseCase {

  private final PayrollClosureRepositoryPort closureRepository;
  private final EventOutboxPort eventOutboxPort;

  public ProcessPayrollClosureUseCase(
      PayrollClosureRepositoryPort closureRepository, EventOutboxPort eventOutboxPort) {
    this.closureRepository = closureRepository;
    this.eventOutboxPort = eventOutboxPort;
  }

  @Override
  @Transactional
  public void execute(UUID runRef, UUID tenantId) {
    PayrollClosure closure = PayrollClosure.initialize(UUID.randomUUID(), runRef, tenantId);

    try {
      closure.seal();
      closureRepository.save(closure);

      PayrollPeriodClosedEvent event =
          new PayrollPeriodClosedEvent(
              runRef, closure.getIntegrityHash(), tenantId, LocalDateTime.now());
      eventOutboxPort.publish(event);

    } catch (Exception e) {
      closure.markAsError();
      closureRepository.save(closure);
      throw new SolverExceptionImpl("PAYROLL_CLOSURE_FAILED");
    }
  }
}
