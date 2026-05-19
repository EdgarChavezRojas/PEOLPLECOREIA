package com.solveria.core.workforce;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solveria.core.dossier.domain.event.DocentAcademicTitleVerifiedEvent;
import com.solveria.core.experience.domain.event.DataChangeRequestedEvent;
import com.solveria.core.legal.domain.event.ContractApprovedEvent;
import com.solveria.core.legal.domain.event.ContractTerminatedEvent;
import com.solveria.core.security.context.SecurityTenantContext;
import com.solveria.core.workforce.application.port.RelationshipRepositoryPort;
import com.solveria.core.workforce.application.usecase.UpdateEmployeeAcademicProfileUseCase;
import com.solveria.core.workforce.application.usecase.UpdatePersonUseCase;
import com.solveria.core.workforce.application.usecase.ReactivateRelationshipUseCase;
import com.solveria.core.workforce.application.usecase.TerminateRelationshipUseCase;
import com.solveria.core.workforce.domain.model.vo.MaritalStatus;
import com.solveria.core.workforce.infrastructure.listener.WorkforceIntegrationConsumer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@Import({WorkforceIntegrationConsumer.class, WorkforceIntegrationEventListenerTest.Config.class})
class WorkforceIntegrationEventListenerTest {

    // Configuración mínima para proveer un ObjectMapper real al contexto de prueba
    @TestConfiguration
    static class Config {
        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    // Mockeamos los Casos de Uso para no ejecutar lógica de dominio ni DB
    @MockitoBean
    private UpdateEmployeeAcademicProfileUseCase updateAcademicRankUseCase;

    @MockitoBean
    private UpdatePersonUseCase updatePersonUseCase;

    @MockitoBean
    private ReactivateRelationshipUseCase reactivateRelationshipUseCase;

    @MockitoBean
    private TerminateRelationshipUseCase terminateRelationshipUseCase;

    @MockitoBean
    private RelationshipRepositoryPort relationshipRepositoryPort;

    @Test
    @DisplayName("Debe procesar DocentAcademicTitleVerifiedEvent y llamar al caso de uso correcto")
    void shouldHandleDocentAcademicTitleVerifiedEvent() {
        // Arrange (Preparar)
        UUID relationshipId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        String titleLevel = "DOCTORADO";

        // Creamos el evento con el relationshipId en lugar del personId
        DocentAcademicTitleVerifiedEvent event = DocentAcademicTitleVerifiedEvent.now(relationshipId, titleLevel, true);

        // Como ya no usamos el repositorio, no necesitamos mockear el Relationship ni el relationshipRepositoryPort.
        // Simulamos el SecurityTenantContext para que devuelva nuestro tenantId de prueba
        //Corregir el tenant id porque no vamos a usar securitycontext para evitar fallos en el hilo secundario
        try (MockedStatic<SecurityTenantContext> mockedSecurityContext = mockStatic(SecurityTenantContext.class)) {
            mockedSecurityContext.when(SecurityTenantContext::getCurrentTenantId).thenReturn(tenantId.toString());

            // Act (Actuar)
            eventPublisher.publishEvent(event);

            // Assert (Verificar)
            // Verificamos que el repositorio ya NUNCA se llame
            verify(relationshipRepositoryPort, never()).findByPersonId(any());

            // Verificamos que el caso de uso se llame directamente con los datos correctos
            verify(updateAcademicRankUseCase, times(1)).execute(relationshipId, tenantId, titleLevel);
        }
    }


    @Test
    @DisplayName("Debe procesar DataChangeRequestedEvent, parsear el JSON y llamar al caso de uso")
    void shouldHandleDataChangeRequestedEvent() {
        // Arrange
        UUID personId = UUID.randomUUID();
        UUID actionId = UUID.randomUUID();

        // Un payload JSON típico que vendría de ESS
        String payload = """
            {
                "maritalStatus": "MARRIED",
                "professionTitle": "Ingeniero de Sistemas"
            }
            """;

        DataChangeRequestedEvent event = new DataChangeRequestedEvent(
                actionId, personId, "DATA_UPDATE", payload, UUID.randomUUID()
        );

        // Act
        eventPublisher.publishEvent(event);

        // Assert
        ArgumentCaptor<MaritalStatus> maritalStatusCaptor = ArgumentCaptor.forClass(MaritalStatus.class);
        ArgumentCaptor<String> professionCaptor = ArgumentCaptor.forClass(String.class);

        verify(updatePersonUseCase, times(1)).execute(
                eq(personId),
                maritalStatusCaptor.capture(),
                professionCaptor.capture(),
                anyList()
        );

        assertEquals(MaritalStatus.MARRIED, maritalStatusCaptor.getValue());
        assertEquals("Ingeniero de Sistemas", professionCaptor.getValue());
    }

    @Test
    @DisplayName("Debe lanzar excepción si el JSON de DataChangeRequestedEvent es inválido")
    void shouldThrowExceptionWhenPayloadIsInvalidJSON() {
        // Arrange
        UUID personId = UUID.randomUUID();
        String invalidPayload = "{ maritalStatus: CASADO "; // JSON roto

        DataChangeRequestedEvent event = new DataChangeRequestedEvent(
                UUID.randomUUID(), personId, "DATA_UPDATE", invalidPayload, UUID.randomUUID()
        );

        // Act & Assert
        // Spring envuelve las excepciones lanzadas por los listeners en un error interno,
        // pero podemos atrapar y verificar que la causa raíz sea nuestra IllegalArgumentException
        Exception exception = assertThrows(Exception.class, () -> eventPublisher.publishEvent(event));

        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("Payload JSON inválido"));

        // Verificamos que no se llamó al caso de uso por haber fallado antes
        verify(updatePersonUseCase, never()).execute(any(), any(), any(), anyList());
    }

    @Test
    @DisplayName("Debe procesar ContractApprovedEvent llamando a ReactivateRelationshipUseCase")
    void shouldHandleContractApprovedEvent() {
        // Arrange
        UUID contractId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        ContractApprovedEvent event = new ContractApprovedEvent(contractId, tenantId, Instant.now());

        // Act
        eventPublisher.publishEvent(event);

        // Assert
        verify(reactivateRelationshipUseCase, times(1)).execute(contractId);
    }

    @Test
    @DisplayName("Debe procesar ContractTerminatedEvent llamando a TerminateRelationshipUseCase con el motivo")
    void shouldHandleContractTerminatedEvent() {
        // Arrange
        UUID contractId = UUID.randomUUID();
        ContractTerminatedEvent event = new ContractTerminatedEvent(contractId, Instant.now());

        // Act
        eventPublisher.publishEvent(event);

        // Assert
        ArgumentCaptor<String> reasonCaptor = ArgumentCaptor.forClass(String.class);
        verify(terminateRelationshipUseCase, times(1)).execute(eq(contractId), reasonCaptor.capture());

        // Validamos que el motivo por defecto se envió correctamente
        assertTrue(reasonCaptor.getValue().contains("Legal/Compliance"));
    }
}