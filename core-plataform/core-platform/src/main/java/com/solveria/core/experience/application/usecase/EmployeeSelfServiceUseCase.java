package com.solveria.core.experience.application.usecase;

import com.solveria.core.experience.application.command.RequestCertificateCommand;
import com.solveria.core.experience.application.command.RequestDataUpdateCommand;
import com.solveria.core.experience.application.command.RequestLeaveCommand;
import com.solveria.core.experience.application.port.in.EmployeeSelfServicePI;
import com.solveria.core.experience.application.port.out.NotificationPO;
import com.solveria.core.experience.application.port.out.PersonLookupPO;
import com.solveria.core.experience.application.port.out.SelfServiceActionPO;
import com.solveria.core.experience.domain.model.Notification;
import com.solveria.core.experience.domain.model.SelfServiceAction;
import com.solveria.core.experience.domain.model.vo.CertificatePayload;
import com.solveria.core.experience.domain.service.CertificateGenerationService;
import com.solveria.core.security.context.SecurityTenantContext;
import com.solveria.core.security.context.SecurityUserContext;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case: Employee Self-Service (ESS). Implementa W11 (Actualización de Datos), W12 (Acuse de
 * Memorandos), W14 (Certificados Digitales), y solicitudes de ausencia.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeSelfServiceUseCase implements EmployeeSelfServicePI {

  private final SelfServiceActionPO selfServiceActionPO;
  private final NotificationPO notificationPO;
  private final PersonLookupPO personLookupPO;

  @Override
  @Transactional
  public UUID requestDataUpdate(RequestDataUpdateCommand cmd) {
    log.info("event=DATA_UPDATE_REQUESTED ");
    UUID tenantIdUUID = UUID.fromString(SecurityTenantContext.getCurrentTenantId());

    SelfServiceAction action =
        SelfServiceAction.requestDataUpdate(
            cmd.personId(), cmd.payload(), tenantIdUUID, cmd.createdBy());

    selfServiceActionPO.save(action);

    log.info("event=DATA_UPDATE_CREATED actionId={} ", action.getActionId());
    return action.getActionId();
  }

  @Override
  @Transactional
  public UUID requestCertificate(
      UUID personId, String certificateType, UUID tenantId, String createdBy) {
    log.info(
        "event=CERTIFICATE_REQUESTED personId={} type={} tenantId={}",
        personId,
        certificateType,
        tenantId);

    RequestCertificateCommand cmd =
        new RequestCertificateCommand(personId, certificateType, createdBy);

    // W14: Generar certificado con hash SHA-256 y QR Zero-Trust
    String pdfContent = "CERT_PLACEHOLDER_" + personId + "_" + certificateType;
    CertificatePayload certPayload =
        CertificateGenerationService.generateCertificate(cmd.certificateType(), pdfContent);

    SelfServiceAction action =
        SelfServiceAction.requestCertificate(
            cmd.personId(), certPayload, tenantId, cmd.createdBy());

    selfServiceActionPO.save(action);

    log.info(
        "event=CERTIFICATE_GENERATED actionId={} sha256={}",
        action.getActionId(),
        certPayload.sha256Hash());
    return action.getActionId();
  }

  @Override
  @Transactional
  public void cancelDataUpdate(UUID actionId, UUID personId, UUID tenantId) {
    log.info(
        "event=DATA_UPDATE_CANCEL_REQUESTED actionId={} personId={} tenantId={}",
        actionId,
        personId,
        tenantId);

    SelfServiceAction action =
        selfServiceActionPO
            .findById(actionId)
            .orElseThrow(
                () -> new IllegalArgumentException("SelfServiceAction no encontrado: " + actionId));

    // Valida autoría y estado PENDING_REVIEW en el dominio
    action.cancel(personId);

    selfServiceActionPO.save(action);

    log.info("event=DATA_UPDATE_CANCELLED actionId={} personId={}", actionId, personId);
  }

  @Override
  @Transactional
  public void acknowledgeNotification(UUID notificationId, UUID personId) {
    log.info(
        "event=MEMORANDUM_ACKNOWLEDGE_REQUESTED notificationId={} personId={}",
        notificationId,
        personId);

    Notification notification =
        notificationPO
            .findById(notificationId)
            .orElseThrow(
                () ->
                    new IllegalArgumentException("Notification no encontrada: " + notificationId));

    // Valida que requiere acknowledgement, no está ya firmada, y es el destinatario
    notification.acknowledge(personId);

    notificationPO.save(notification);

    log.info(
        "event=MEMORANDUM_ACKNOWLEDGED notificationId={} personId={} acknowledgedAt={}",
        notificationId,
        personId,
        notification.getAcknowledgedAt());
  }

  @Override
  @Transactional
  public UUID requestLeave(RequestLeaveCommand cmd) {
    UUID tenantIdUUID = UUID.fromString(SecurityTenantContext.getCurrentTenantId());
    Long userId = SecurityUserContext.getUserId();
    UUID personId =
        personLookupPO
            .findPersonIdByUserId(userId)
            .orElseThrow(
                () -> new IllegalArgumentException("Person no encontrada para userId: " + userId));

    log.info(
        "event=LEAVE_REQUESTED personId={} leaveType={} start={} end={} ",
        personId,
        cmd.leaveType(),
        cmd.startDate(),
        cmd.endDate());

    SelfServiceAction action =
        SelfServiceAction.requestLeave(
            personId,
            cmd.leaveType(),
            cmd.startDate(),
            cmd.endDate(),
            tenantIdUUID,
            personId.toString());

    selfServiceActionPO.save(action);

    log.info(
        "event=LEAVE_REQUEST_CREATED actionId={} personId={} leaveType={}",
        action.getActionId(),
        personId,
        cmd.leaveType());
    return action.getActionId();
  }

  @Override
  @Transactional(readOnly = true)
  public BigDecimal getAvailableLeaveBalance() {
    Long userId = SecurityUserContext.getUserId();
    UUID personId =
        personLookupPO
            .findPersonIdByUserId(userId)
            .orElseThrow(
                () -> new IllegalArgumentException("Person no encontrada para userId: " + userId));

    log.info("event=LEAVE_BALANCE_QUERY_REQUESTED personId={}", personId);

    BigDecimal balance = selfServiceActionPO.getAvailableLeaveBalance(personId);

    log.info("event=LEAVE_BALANCE_QUERY_SUCCESS personId={} balance={}", personId, balance);
    return balance;
  }
}
