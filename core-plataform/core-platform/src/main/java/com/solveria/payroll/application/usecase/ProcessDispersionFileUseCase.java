package com.solveria.payroll.application.usecase;

import com.solveria.payroll.application.port.inbound.GenerateDispersionFileUseCase;
import com.solveria.payroll.application.port.outbound.BankDispersionFileRepositoryPort;
import com.solveria.payroll.application.port.outbound.EmployeeBankValidationPort;
import com.solveria.payroll.domain.model.ar.BankDispersionFile;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProcessDispersionFileUseCase implements GenerateDispersionFileUseCase {

  private final BankDispersionFileRepositoryPort repository;
  private final EmployeeBankValidationPort validationPort;

  public ProcessDispersionFileUseCase(
      BankDispersionFileRepositoryPort repository, EmployeeBankValidationPort validationPort) {
    this.repository = repository;
    this.validationPort = validationPort;
  }

  @Override
  @Transactional
  public void execute(UUID runRef, UUID bankEntityRef, UUID tenantId) {
    boolean allHaveAccounts = validationPort.allEmployeesHaveBankAccount(runRef, tenantId);

    BankDispersionFile file = new BankDispersionFile();
    file.setId(UUID.randomUUID());
    file.setTenantId(tenantId);
    file.setRunRef(runRef);
    file.setBankEntityRef(bankEntityRef);
    file.setTotalAmount(BigDecimal.ZERO);
    file.setRecordCount(0);

    file.generate(allHaveAccounts);

    repository.save(file);
  }
}
