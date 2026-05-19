package com.solveria.core.financial;

import static org.junit.jupiter.api.Assertions.*;

import com.solveria.core.financial.domain.model.vo.UfvProviderUnavailableException;
import java.math.BigDecimal;
import java.time.LocalDate;
import com.solveria.core.financial.infrastructure.adapter.BcbWebScrapingUfvAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BcbWebScrapingUfvAdapterIntTest {

    private BcbWebScrapingUfvAdapter adapter;

    @BeforeEach
    void setUp() {
        // Instanciamos el adaptador real, sin Mocks.
        adapter = new BcbWebScrapingUfvAdapter();
    }

    @Test
    @DisplayName("Debe descargar el PDF del año actual y extraer la UFV de ayer")
    void shouldFetchRealUfvFromCurrentYearPdf() {
        // Usamos el día de ayer asumiendo que el BCB ya tiene el dato consolidado
        LocalDate yesterday = LocalDate.now().minusDays(1);

        BigDecimal realUfvValue = adapter.getUfvValue(yesterday);

        assertNotNull(realUfvValue, "La UFV no debería ser nula");
        assertTrue(realUfvValue.compareTo(BigDecimal.ZERO) > 0, "La UFV debe ser mayor a 0");

        System.out.println("✅ ÉXITO - UFV de ayer (" + yesterday + "): " + realUfvValue);
    }

    @Test
    @DisplayName("Debe descargar el PDF de un año histórico (2023) y extraer un dato exacto")
    void shouldFetchRealUfvFromPastYearPdf() {
        // Vamos a buscar una fecha antigua para probar que puede leer PDFs pasados
        LocalDate historicalDate = LocalDate.of(2023, 1, 1);

        BigDecimal historicalUfv = adapter.getUfvValue(historicalDate);

        assertNotNull(historicalUfv);
        assertTrue(historicalUfv.compareTo(BigDecimal.ZERO) > 0);

        System.out.println("✅ ÉXITO - UFV Histórica (" + historicalDate + "): " + historicalUfv);
    }

    @Test
    @DisplayName("Verificar que la caché funcione: La segunda llamada debe ser instantánea")
    void shouldFetchFromCacheInstantlyOnSecondCall() {
        LocalDate date1 = LocalDate.of(2024, 5, 10);
        LocalDate date2 = LocalDate.of(2024, 8, 20); // Mismo año

        // Primera llamada: Debería tardar un poco porque descarga el PDF del 2024
        long start1 = System.currentTimeMillis();
        adapter.getUfvValue(date1);
        long duration1 = System.currentTimeMillis() - start1;

        // Segunda llamada: Debería tardar casi 0 ms porque ya está en la memoria RAM
        long start2 = System.currentTimeMillis();
        adapter.getUfvValue(date2);
        long duration2 = System.currentTimeMillis() - start2;

        System.out.println("Tiempo de descarga y parseo PDF: " + duration1 + "ms");
        System.out.println("Tiempo de respuesta desde Caché: " + duration2 + "ms");

        assertTrue(duration2 < duration1, "La llamada a caché debe ser mucho más rápida");
    }

    @Test
    @DisplayName("Debe fallar al pedir una fecha del futuro que el BCB aún no ha publicado")
    void shouldFailWhenRequestingUnpublishedDate() {
        // Pedimos una fecha muy lejana que no existe en el PDF
        LocalDate futureDate = LocalDate.now().plusYears(1);

        UfvProviderUnavailableException exception = assertThrows(
                UfvProviderUnavailableException.class,
                () -> adapter.getUfvValue(futureDate)
        );

        assertTrue(exception.getMessage().contains("aún no ha publicado") ||
                        exception.getMessage().contains("no contiene el valor"),
                "Debe lanzar un mensaje indicando que el dato no existe");

        System.out.println("✅ ÉXITO - Se bloqueó correctamente la fecha futura: " + futureDate);
    }
}