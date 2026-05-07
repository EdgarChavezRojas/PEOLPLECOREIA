package com.solveria.core.experience.application.usecase;

import com.solveria.core.experience.application.command.RequestCertificateCommand;
import com.solveria.core.experience.application.command.RequestDataUpdateCommand;
import com.solveria.core.experience.application.port.in.EmployeeSelfServicePI;
import com.solveria.core.experience.application.port.out.SelfServiceActionPO;
import com.solveria.core.experience.domain.model.SelfServiceAction;
import com.solveria.core.experience.domain.model.vo.CertificatePayload;
import com.solveria.core.experience.domain.service.CertificateGenerationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case: Employee Self-Service (ESS). Implementa W11 (Actualización de Datos) y W14
 * (Certificados Digitales).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeSelfServiceUseCase implements EmployeeSelfServicePI {

  private final SelfServiceActionPO selfServiceActionPO;

  @Override
  @Transactional
  public UUID requestDataUpdate(UUID personId, String payload, String tenantId, String createdBy) {
    log.info("event=DATA_UPDATE_REQUESTED personId={} tenantId={}", personId, tenantId);

    RequestDataUpdateCommand cmd =
        new RequestDataUpdateCommand(personId, payload, tenantId, createdBy);

    SelfServiceAction action =
        SelfServiceAction.requestDataUpdate(
            cmd.personId(), cmd.payload(), cmd.tenantId(), cmd.createdBy());

    selfServiceActionPO.save(action);

    log.info("event=DATA_UPDATE_CREATED actionId={} personId={}", action.getActionId(), personId);
    return action.getActionId();
  }

  @Override
  @Transactional
  public UUID requestCertificate(
      UUID personId, String certificateType, String tenantId, String createdBy) {
    log.info(
        "event=CERTIFICATE_REQUESTED personId={} type={} tenantId={}",
        personId,
        certificateType,
        tenantId);

    RequestCertificateCommand cmd =
        new RequestCertificateCommand(personId, certificateType, tenantId, createdBy);

    // W14: Generar certificado con hash SHA-256 y QR Zero-Trust
    String pdfContent = "CERT_PLACEHOLDER_" + personId + "_" + certificateType;
    CertificatePayload certPayload =
        CertificateGenerationService.generateCertificate(cmd.certificateType(), pdfContent);

    SelfServiceAction action =
        SelfServiceAction.requestCertificate(
            cmd.personId(), certPayload, cmd.tenantId(), cmd.createdBy());

    selfServiceActionPO.save(action);

    log.info(
        "event=CERTIFICATE_GENERATED actionId={} sha256={}",
        action.getActionId(),
        certPayload.sha256Hash());
    return action.getActionId();
  }
}
