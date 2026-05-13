package com.solveria.core.legal.application.usecase;

import com.solveria.core.legal.application.port.ContractRepositoryPort;
import com.solveria.core.legal.domain.model.Contract;
import com.solveria.core.legal.domain.model.vo.ContractType;
import com.solveria.core.security.context.SecurityTenantContext;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScanExpiringContractsUseCase {

  private final ContractRepositoryPort contractRepositoryPort;
  private final Clock clock;

  @Transactional
  public void execute() {
    String tenantId = SecurityTenantContext.getCurrentTenantId();
    LocalDate exactDate = LocalDate.now(clock).plusDays(90);

    List<Contract> contracts =
        contractRepositoryPort.findContractsExpiringExactlyOn(
            ContractType.PLAZO_FIJO, exactDate, tenantId);

    int alertsSent = 0;
    for (Contract contract : contracts) {
      if (contract.isTacitaReconduccionAlertSent()) {
        continue;
      }
      contract.markTacitaReconduccionRisk();
      contract.markTacitaReconduccionAlertSent();
      contractRepositoryPort.save(contract);
      alertsSent++;
    }

    log.info(
        "event=LEGAL_CONTRACT_TACITA_SCAN_SUCCESS tenantId={} alertsSent={}",
        tenantId,
        alertsSent);
  }
}

