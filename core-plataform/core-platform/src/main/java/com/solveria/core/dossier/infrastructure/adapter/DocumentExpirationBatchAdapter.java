package com.solveria.core.dossier.infrastructure.adapter;

import com.solveria.core.dossier.application.command.EvaluateDocumentExpirationsCommand;
import com.solveria.core.dossier.application.usecase.EvaluateDocumentExpirationsUseCase;
import java.time.LocalDate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DocumentExpirationBatchAdapter {

  private final EvaluateDocumentExpirationsUseCase evaluateDocumentExpirationsUseCase;

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
  @Scheduled(cron = /*DossierCronProperties.DOCUMENT_EXPIRATION_CRON*/ "0 0 0 * * ?")
  public void run() {
    EvaluateDocumentExpirationsCommand command =
        new EvaluateDocumentExpirationsCommand(LocalDate.now(), null);
    evaluateDocumentExpirationsUseCase.handle(command);
  }
}
