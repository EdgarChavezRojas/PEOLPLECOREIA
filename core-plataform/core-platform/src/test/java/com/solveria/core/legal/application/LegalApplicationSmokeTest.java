package com.solveria.core.legal.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.solveria.core.legal.application.dto.ComplianceSnapshotDto;
import com.solveria.core.legal.application.dto.DraftContractRequest;
import com.solveria.core.legal.application.dto.ProposeContractAddendumRequest;
import com.solveria.core.legal.application.dto.SalaryTermsDto;
import com.solveria.core.legal.application.port.ContractRepositoryPort;
import com.solveria.core.legal.application.scheduler.TacitaReconduccionMonitor;
import com.solveria.core.legal.application.usecase.DraftContractUseCase;
import com.solveria.core.legal.application.usecase.ProposeContractAddendumUseCase;
import com.solveria.core.legal.domain.model.Contract;
import com.solveria.core.legal.domain.model.vo.ContractType;
import com.solveria.core.legal.domain.model.vo.EmploymentCondition;
import com.solveria.core.security.context.SecurityTenantContext;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LegalApplicationSmokeTest {

  private static final String TENANT_ID = "tenant-1";

  private InMemoryContractRepository contractRepository;
  @BeforeEach
  void setUp() {
    SecurityTenantContext.setTenantId(TENANT_ID);
    contractRepository = new InMemoryContractRepository();
  }

  @AfterEach
  void tearDown() {
    SecurityTenantContext.clear();
  }

  @Test
  void draftProposeAndMonitorFlow() {
    DraftContractUseCase draftUseCase = new DraftContractUseCase(contractRepository);
    ProposeContractAddendumUseCase proposeUseCase =
        new ProposeContractAddendumUseCase(contractRepository, null);
    TacitaReconduccionMonitor monitor = new TacitaReconduccionMonitor(contractRepository);

    UUID relationshipId = UUID.randomUUID();
    DraftContractRequest draftRequest =
        new DraftContractRequest(
            null, relationshipId, ContractType.PLAZO_FIJO, EmploymentCondition.PE,"PRJ-001", TENANT_ID);

    UUID contractId = draftUseCase.execute(draftRequest).contractId();
    assertNotNull(contractId);

    ProposeContractAddendumRequest addendumRequest =
        new ProposeContractAddendumRequest(
            contractId,
            null,
            LocalDate.now(),
            LocalDate.now().plusDays(90),
            new SalaryTermsDto(
                new BigDecimal("3300"), new BigDecimal("3300"), new BigDecimal("3000"), "BOB"),
            new ComplianceSnapshotDto(new BigDecimal("3300"), "RC-IVA", Boolean.TRUE),
            TENANT_ID);

    proposeUseCase.execute(addendumRequest);

    monitor.run();

    Contract stored = contractRepository.findById(contractId).orElseThrow();
    assertEquals(relationshipId, stored.getRelationshipId());
    assertEquals(1, stored.getAddendums().size());
  }

  private static class InMemoryContractRepository implements ContractRepositoryPort {

    private final Map<UUID, Contract> storage = new HashMap<>();

    @Override
    public Optional<Contract> findById(UUID contractId) {
      return Optional.ofNullable(storage.get(contractId));
    }

    @Override
    public void save(Contract contract) {
      storage.put(contract.getContractId(), contract);
    }

    @Override
    public List<Contract> findFixedTermContractsExpiringBetween(LocalDate from, LocalDate to) {
      List<Contract> result = new ArrayList<>();
      for (Contract contract : storage.values()) {
        if (contract.getContractType() != ContractType.PLAZO_FIJO) {
          continue;
        }
        boolean matches =
            contract.getAddendums().stream()
                .anyMatch(
                    addendum ->
                        addendum.getEffectiveTo() != null
                            && !addendum.getEffectiveTo().isBefore(from)
                            && !addendum.getEffectiveTo().isAfter(to));
        if (matches) {
          result.add(contract);
        }
      }
      return result;
    }
  }
}
