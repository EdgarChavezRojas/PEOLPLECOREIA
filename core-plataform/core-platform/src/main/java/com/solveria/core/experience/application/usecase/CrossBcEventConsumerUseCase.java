package com.solveria.core.experience.application.usecase;

import com.solveria.core.experience.application.port.out.NotificationPO;
import com.solveria.core.experience.application.port.out.PredictionModelPO;
import com.solveria.core.experience.domain.model.Notification;
import com.solveria.core.experience.domain.model.PredictionModel;
import com.solveria.core.experience.domain.model.vo.NotificationChannel;
import com.solveria.core.experience.domain.model.vo.RiskAlert;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case: Consumidor de eventos cross-BC. CONSUMERS: - QUINQUENIO_PAYMENT_OVERDUE -> Creates
 * CRITICAL RiskAlert. - DOCUMENT_VALIDATION_REJECTED -> Creates Push Notification to user. -
 * ELIGIBILITY_SUSPENDED_BY_COMPLIANCE -> Creates Alert for Store Manager.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CrossBcEventConsumerUseCase {

  private final PredictionModelPO predictionModelPO;
  private final NotificationPO notificationPO;

  /** Consumer: QUINQUENIO_PAYMENT_OVERDUE -> CRITICAL RiskAlert */
  @Transactional
  public void handleQuinquenioPaymentOverdue(
      UUID modelId, UUID personId, BigDecimal amount, String tenantId) {
    log.info("event=CONSUME_QUINQUENIO_PAYMENT_OVERDUE personId={} amount={}", personId, amount);

    PredictionModel model =
        predictionModelPO
            .findById(modelId)
            .orElseThrow(
                () -> new IllegalArgumentException("PredictionModel no encontrado: " + modelId));

    RiskAlert alert = model.handleQuinquenioPaymentOverdue(personId, amount);
    predictionModelPO.save(model);

    log.info(
        "event=QUINQUENIO_OVERDUE_ALERT_CREATED alertId={} severity={}",
        alert.alertId(),
        alert.severity());
  }

  /** Consumer: DOCUMENT_VALIDATION_REJECTED -> Push Notification to user */
  @Transactional
  public void handleDocumentValidationRejected(
      UUID personId, String documentType, String reason, String tenantId) {
    log.info(
        "event=CONSUME_DOCUMENT_VALIDATION_REJECTED personId={} docType={}",
        personId,
        documentType);

    Notification notification =
        Notification.send(
            personId,
            NotificationChannel.PUSH_MOBILE,
            "Documento Rechazado: " + documentType,
            "Su documento " + documentType + " fue rechazado. Motivo: " + reason,
            tenantId);

    notificationPO.save(notification);

    log.info(
        "event=DOC_REJECTED_NOTIFICATION_SENT notifId={} recipientId={}",
        notification.getNotificationId(),
        personId);
  }

  /** Consumer: ELIGIBILITY_SUSPENDED_BY_COMPLIANCE -> Alert for Store Manager */
  @Transactional
  public void handleEligibilitySuspended(
      UUID modelId, UUID personId, String reason, String tenantId) {
    log.info("event=CONSUME_ELIGIBILITY_SUSPENDED personId={} reason={}", personId, reason);

    PredictionModel model =
        predictionModelPO
            .findById(modelId)
            .orElseThrow(
                () -> new IllegalArgumentException("PredictionModel no encontrado: " + modelId));

    RiskAlert alert = model.handleEligibilitySuspended(personId, reason);
    predictionModelPO.save(model);

    log.info(
        "event=ELIGIBILITY_SUSPENDED_ALERT_CREATED alertId={} severity={}",
        alert.alertId(),
        alert.severity());
  }
}
