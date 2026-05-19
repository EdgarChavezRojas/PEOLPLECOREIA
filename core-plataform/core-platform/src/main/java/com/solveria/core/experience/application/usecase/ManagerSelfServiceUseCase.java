package com.solveria.core.experience.application.usecase;

import com.solveria.core.experience.application.command.ApproveDataChangeCommand;
import com.solveria.core.experience.application.command.RejectDataChangeCommand;
import com.solveria.core.experience.application.port.in.ManagerSelfServicePI;
import com.solveria.core.experience.application.port.out.SelfServiceActionPO;
import com.solveria.core.experience.domain.model.SelfServiceAction;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case: Manager Self-Service (MSS). Implementa aprobación/rechazo de solicitudes ESS (W11).
 * Invariante SoD: el aprobador NO puede ser el mismo solicitante.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ManagerSelfServiceUseCase implements ManagerSelfServicePI {

  private final SelfServiceActionPO selfServiceActionPO;

  @Override
  @Transactional
  public void approveDataChange(UUID actionId, UUID approvedBy, String tenantId) {
    log.info("event=DATA_CHANGE_APPROVAL actionId={} approvedBy={}", actionId, approvedBy);

    ApproveDataChangeCommand cmd = new ApproveDataChangeCommand(actionId, approvedBy);

    SelfServiceAction action =
        selfServiceActionPO
            .findById(cmd.actionId())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "SelfServiceAction no encontrada: " + cmd.actionId()));

    // Invariante SoD se valida dentro del dominio
    action.approveDataChange(cmd.approvedBy());

    selfServiceActionPO.save(action);

    log.info("event=DATA_CHANGE_APPROVED actionId={} approvedBy={}", actionId, approvedBy);
  }

  @Override
  @Transactional
  public void rejectDataChange(
      UUID actionId, UUID rejectedBy, String rejectionReason, String tenantId) {
    log.info("event=DATA_CHANGE_REJECTION actionId={} rejectedBy={}", actionId, rejectedBy);

    RejectDataChangeCommand cmd =
        new RejectDataChangeCommand(actionId, rejectedBy, rejectionReason);

    SelfServiceAction action =
        selfServiceActionPO
            .findById(cmd.actionId())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "SelfServiceAction no encontrada: " + cmd.actionId()));

    // Invariante SoD se valida dentro del dominio
    action.rejectDataChange(cmd.rejectedBy(), cmd.rejectionReason());

    selfServiceActionPO.save(action);

    log.info(
        "event=DATA_CHANGE_REJECTED actionId={} rejectedBy={} reason={}",
        actionId,
        rejectedBy,
        rejectionReason);
  }
}
