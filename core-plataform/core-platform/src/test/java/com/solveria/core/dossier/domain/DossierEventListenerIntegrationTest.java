// Ruta:
// core-plataform/core-platform/src/test/java/com/solveria/core/dossier/domain/DossierEventListenerIntegrationTest.java
package com.solveria.core.dossier.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.solveria.core.dossier.application.command.ArchiveContractCommand;
import com.solveria.core.dossier.application.command.CreateDocumentRequirementsCommand;
import com.solveria.core.dossier.application.port.AssignedAssetRepositoryPort;
import com.solveria.core.dossier.application.usecase.*;
import com.solveria.core.dossier.infrastructure.listener.DossierEventListener;
import com.solveria.core.legal.domain.event.ContractApprovedEvent;
import com.solveria.core.workforce.domain.event.PersonMasterCreatedEvent;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(classes = DossierEventListener.class)
class DossierEventListenerIntegrationTest {

  @Autowired private ApplicationEventPublisher eventPublisher;

  @MockitoBean private CreateDocumentRequirementsUseCase createDocumentRequirementsUseCase;

  @MockitoBean private ArchiveContractUseCase archiveContractUseCase;

  @MockitoBean private ReturnAssetUseCase returnAssetUseCase;

  @MockitoBean private RecordDisciplinaryActionUseCase recordDisciplinaryActionUseCase;

  @MockitoBean private AcknowledgeMemorandumUseCase acknowledgeMemorandumUseCase;

  // 3. Y no olvides el repositorio que añadimos manualmente para corregir el "Bug A" de los
  // activos:
  @MockitoBean private AssignedAssetRepositoryPort assignedAssetRepositoryPort;

  @Test
  @DisplayName(
      "Debe consumir PersonMasterCreatedEvent y delegar en CreateDocumentRequirementsUseCase")
  void shouldHandlePersonMasterCreatedEvent() {
    UUID personId = UUID.randomUUID();
    UUID tenantId = UUID.randomUUID();

    PersonMasterCreatedEvent event =
        new PersonMasterCreatedEvent(personId, tenantId, Instant.now());

    // Este test valida que el listener no depende de contexto HTTP y es resiliente al Outbox.
    eventPublisher.publishEvent(event);

    ArgumentCaptor<CreateDocumentRequirementsCommand> captor =
        ArgumentCaptor.forClass(CreateDocumentRequirementsCommand.class);
    verify(createDocumentRequirementsUseCase, times(1)).handle(captor.capture());

    CreateDocumentRequirementsCommand command = captor.getValue();
    assertEquals(personId, command.workerId());
    assertEquals(tenantId, command.tenantId());
  }

  @Test
  @DisplayName("Debe consumir ContractApprovedEvent y delegar en ArchiveContractUseCase")
  void shouldHandleContractApprovedEvent() {
    UUID contractId = UUID.randomUUID();
    UUID tenantId = UUID.randomUUID();

    ContractApprovedEvent event = new ContractApprovedEvent(contractId, tenantId, Instant.now());

    // Este test valida que el listener no depende de contexto HTTP y es resiliente al Outbox.
    eventPublisher.publishEvent(event);

    ArgumentCaptor<ArchiveContractCommand> captor =
        ArgumentCaptor.forClass(ArchiveContractCommand.class);
    verify(archiveContractUseCase, times(1)).handle(captor.capture());

    ArchiveContractCommand command = captor.getValue();
    assertEquals(contractId, command.workerId());
    assertEquals(contractId, command.contractId());
    assertEquals("EVIDENCIA_CONTRATO_WORM", command.contractReference());
    assertEquals(tenantId, command.tenantId());
  }
}
