package com.solveria.core.legal.application.usecase;

import com.solveria.core.legal.application.dto.ContractEvidenceResponse;
import com.solveria.core.legal.application.dto.GenerateContractEvidenceRequest;
import com.solveria.core.legal.application.port.AuditLogPort;
import com.solveria.core.legal.application.port.ContractRepositoryPort;
import com.solveria.core.legal.application.port.DigitalKardexPort;
import com.solveria.core.legal.domain.event.EvidenceGeneratedEvent;
import com.solveria.core.legal.domain.exception.ContractNotFoundException;
import com.solveria.core.legal.domain.exception.EvidenceDataMissingException;
import com.solveria.core.legal.domain.model.Contract;
import com.solveria.core.legal.domain.model.ContractAddendum;
import com.solveria.core.legal.domain.model.vo.AddendumStatus;
import com.solveria.core.legal.domain.model.vo.SalaryTerms;
import com.solveria.core.shared.outbox.application.port.EventOutboxPort;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenerateContractEvidenceUseCase {

  private final ContractRepositoryPort contractRepositoryPort;
  private final DigitalKardexPort digitalKardexPort;
  private final AuditLogPort auditLogPort;
  private final EventOutboxPort eventOutboxPort;
  private final Clock clock;

  @Transactional
  public ContractEvidenceResponse execute(GenerateContractEvidenceRequest request) {

    Contract contract =
        contractRepositoryPort
            .findById(request.contractId())
            .orElseThrow(() -> new ContractNotFoundException(request.contractId()));

    EvidenceData evidenceData = resolveEvidenceData(contract);
    Instant generatedAt = Instant.now(clock);

    // CAMBIO 1: Solo extraemos el contenido crudo (bytes), la capa de aplicación ya no sabe de
    // "SHA-256"
    byte[] fileContent =
        buildRawEvidenceContent(
            contract.getContractId(),
            evidenceData.salaryTerms,
            evidenceData.startDate,
            request.tenantId(),
            generatedAt);

    // CAMBIO 2: Pasamos los bytes al puerto y este nos DEVUELVE el hash oficial del Kardex
    String hash =
        digitalKardexPort.storeEvidence(
            contract.getContractId(), request.tenantId(), fileContent, generatedAt);

    auditLogPort.registerEvidenceGenerated(contract.getContractId(), generatedAt, hash);
    eventOutboxPort.publish(
        List.of(
            new EvidenceGeneratedEvent(
                contract.getContractId(), request.tenantId(), hash, generatedAt)));

    log.info(
        "event=LEGAL_CONTRACT_EVIDENCE_SUCCESS contractId={} tenantId={}",
        contract.getContractId(),
        request.tenantId());

    return new ContractEvidenceResponse(contract.getContractId(), hash, generatedAt);
  }

  private EvidenceData resolveEvidenceData(Contract contract) {
    // ... se mantiene exactamente igual ...
    Optional<ContractAddendum> latestApproved =
        contract.getAddendums().stream()
            .filter(addendum -> addendum.getStatus() == AddendumStatus.APPROVED)
            .max(Comparator.comparing(ContractAddendum::getEffectiveFrom));

    if (latestApproved.isEmpty()) {
      throw new EvidenceDataMissingException(contract.getContractId());
    }

    ContractAddendum addendum = latestApproved.get();
    if (addendum.getSalaryTerms() == null || addendum.getSalaryTerms().basicSalary() == null) {
      throw new EvidenceDataMissingException(contract.getContractId());
    }

    return new EvidenceData(addendum.getSalaryTerms(), addendum.getEffectiveFrom());
  }

  // CAMBIO 3: Reemplazamos buildSha256Hash por este método simple que solo devuelve el payload en
  // bytes
  private byte[] buildRawEvidenceContent(
      java.util.UUID contractId,
      SalaryTerms salaryTerms,
      LocalDate startDate,
      UUID tenantId,
      Instant generatedAt) {

    // String.join es la mejor práctica en Java para unir elementos con un delimitador específico.
    String payload =
        String.join(
            "|",
            contractId.toString(),
            salaryTerms.basicSalary().toString(),
            startDate.toString(),
            tenantId.toString(),
            generatedAt.toString());

    return payload.getBytes(StandardCharsets.UTF_8);
  }

  private record EvidenceData(SalaryTerms salaryTerms, LocalDate startDate) {}
}
