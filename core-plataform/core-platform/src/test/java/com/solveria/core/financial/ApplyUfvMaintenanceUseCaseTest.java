package com.solveria.core.financial;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.solveria.core.financial.application.port.UfvQuotationPort;
import com.solveria.core.financial.application.usecase.ApplyUfvMaintenanceUseCase;
import com.solveria.core.financial.domain.model.vo.UfvProviderUnavailableException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ApplyUfvMaintenanceUseCaseTest {

  @Mock private UfvQuotationPort ufvQuotationPort;

  @InjectMocks private ApplyUfvMaintenanceUseCase useCase;

  private final LocalDate startDate = LocalDate.of(2026, 1, 30);
  private final LocalDate endDate = LocalDate.of(2026, 1, 31);
  private final String tenantId = "TENANT-RETAIL-01";

  @BeforeEach
  void setUp() {
    // Limpiamos los eventos usando el método real de tu DomainRoot
    // Esto asegura que la lista empiece en 0 antes de cada test
    useCase.pullDomainEvents();
  }

  @Test
  @DisplayName("Debe calcular correctamente el mantenimiento de valor e incrementar saldos")
  void shouldCalculateUfvMaintenanceSuccessfully() {
    // Given (Datos de preparación)
    BigDecimal provisionBalance = new BigDecimal("10000.00");
    BigDecimal fiscalCredit = new BigDecimal("5000.00");

    // Simulamos que el adaptador del BCB responde con UFVs de la vida real
    when(ufvQuotationPort.getUfvValue(startDate)).thenReturn(new BigDecimal("2.50000"));
    when(ufvQuotationPort.getUfvValue(endDate)).thenReturn(new BigDecimal("2.51250"));

    // When (Ejecución del Use Case)
    ApplyUfvMaintenanceUseCase.UfvMaintenanceResult result =
        useCase.execute(startDate, endDate, provisionBalance, fiscalCredit, tenantId);

    // Then (Verificaciones matemáticas)
    assertNotNull(result);
    assertEquals(new BigDecimal("10050.00"), result.updatedProvisionBalance()); // 10000 * 1.005
    assertEquals(new BigDecimal("5025.00"), result.updatedFiscalCredit()); // 5000 * 1.005

    // Verificamos eventos de dominio usando tu DomainRoot real
    // Al llamar a pullDomainEvents(), extraemos la lista y la limpiamos del Root
    List<?> dispatchedEvents = useCase.pullDomainEvents();
    assertEquals(1, dispatchedEvents.size(), "Debe registrarse exactamente 1 evento de dominio");

    verify(ufvQuotationPort, times(1)).getUfvValue(startDate);
    verify(ufvQuotationPort, times(1)).getUfvValue(endDate);
  }

  @Test
  @DisplayName("Debe lanzar excepción y detener el proceso si el BCB no responde")
  void shouldThrowExceptionWhenBcbIsDown() {
    // Given
    BigDecimal provisionBalance = new BigDecimal("10000.00");
    BigDecimal fiscalCredit = new BigDecimal("5000.00");

    // Simulamos que el adaptador lanza la excepción (timeout o no encuentra el regex)
    when(ufvQuotationPort.getUfvValue(startDate))
        .thenThrow(new UfvProviderUnavailableException("Timeout 5s BCB"));

    // When & Then
    UfvProviderUnavailableException exception =
        assertThrows(
            UfvProviderUnavailableException.class,
            () -> useCase.execute(startDate, endDate, provisionBalance, fiscalCredit, tenantId));

    assertTrue(exception.getMessage().contains("Timeout"));

    // Verificamos que NO se registraron eventos fantasma en el DomainRoot
    List<?> dispatchedEvents = useCase.pullDomainEvents();
    assertEquals(0, dispatchedEvents.size(), "No debe haber eventos si el proceso falló");

    // Verificamos que no intentó buscar la UFV final si la inicial ya falló
    verify(ufvQuotationPort, never()).getUfvValue(endDate);
  }
}
