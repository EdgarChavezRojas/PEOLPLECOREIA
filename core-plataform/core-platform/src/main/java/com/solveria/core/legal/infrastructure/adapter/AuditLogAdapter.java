package com.solveria.core.legal.infrastructure.adapter;

import com.solveria.core.audit.domain.event.AuditEvent;
import com.solveria.core.legal.application.port.AuditLogPort;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Component
public class AuditLogAdapter implements AuditLogPort {

    private final ApplicationEventPublisher eventPublisher;

    // Plantillas constantes para el campo 'action'.
    // Mantenemos la legibilidad y aseguramos que el detalle importante se guarde.
    private static final String ACTION_EVIDENCE_GENERATED = "EVIDENCE_GENERATED | Hash: %s";
    private static final String ACTION_THRESHOLD_UPDATED = "LEGAL_THRESHOLD_UPDATED | Regla: %s | %s -> %s";

    private static final String ENTITY_CONTRACT = "Contract";
    private static final String ENTITY_POLICY_RULE = "PolicyRule";

    public AuditLogAdapter(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void registerEvidenceGenerated(
            UUID contractId, String tenantId, String userId, Instant generatedAt, String hash) {

        // Formateamos la acción para incluir el hash dentro del límite de los 4 parámetros
        String actionDescription = String.format(ACTION_EVIDENCE_GENERATED, hash);

        // Instanciamos el evento SOLO con los 4 argumentos que exige tu 'record'
        AuditEvent event = new AuditEvent(
                actionDescription,
                ENTITY_CONTRACT,
                contractId.toString(),
                generatedAt
        );

        eventPublisher.publishEvent(event);

        // Nota: tenantId y userId son ignorados aquí porque tu AuditEventListener
        // los inyecta automáticamente llamando a SecurityTenantContext y SecurityUserContext.
    }

    @Override
    public void registerLegalThresholdUpdate(
            UUID policyRuleId, String ruleName, BigDecimal previousValue,
            BigDecimal newValue, String userId, Instant occurredAt) {

        // Formateamos la acción para mostrar la trazabilidad del cambio
        String actionDescription = String.format(ACTION_THRESHOLD_UPDATED, ruleName, previousValue, newValue);

        AuditEvent event = new AuditEvent(
                actionDescription,
                ENTITY_POLICY_RULE,
                policyRuleId.toString(),
                occurredAt
        );

        eventPublisher.publishEvent(event);
    }
}