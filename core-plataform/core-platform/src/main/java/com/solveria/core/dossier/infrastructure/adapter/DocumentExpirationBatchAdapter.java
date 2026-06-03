package com.solveria.core.dossier.infrastructure.adapter;

import com.solveria.core.dossier.application.command.EvaluateDocumentExpirationsCommand;
import com.solveria.core.dossier.application.usecase.EvaluateDocumentExpirationsUseCase;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DocumentExpirationBatchAdapter {

  private final EvaluateDocumentExpirationsUseCase evaluateDocumentExpirationsUseCase;
  private static final Logger log = LoggerFactory.getLogger(DocumentExpirationBatchAdapter.class);

  public DocumentExpirationBatchAdapter(
      EvaluateDocumentExpirationsUseCase evaluateDocumentExpirationsUseCase) {
    this.evaluateDocumentExpirationsUseCase = evaluateDocumentExpirationsUseCase;
  }

  /**
   * CRON JOB GLOBAL (Multi-Tenant) Este proceso se ejecuta en un hilo en segundo plano (background
   * thread) gestionado por Spring. IMPORTANTE: Al no provenir de una petición HTTP, no existe un
   * token JWT ni un usuario logueado. Por lo tanto, el SecurityTenantContext estará vacío. Este
   * comando lanza una evaluación global para todos los tenants del sistema.
   */
  @Scheduled(cron = "${dossier.cron.document-expiration}")
  public void run() {
    log.info(
        "event=DOSSIER_DOC_EXPIRATION_BATCH_START cron={} today={}",
        "${dossier.cron.document-expiration}",
        LocalDate.now());
    EvaluateDocumentExpirationsCommand command =
        new EvaluateDocumentExpirationsCommand(LocalDate.now());
    evaluateDocumentExpirationsUseCase.handle(command);
    log.info("event=DOSSIER_DOC_EXPIRATION_BATCH_END");
  }
}
