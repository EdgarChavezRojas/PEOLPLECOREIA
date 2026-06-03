package com.solveria.ai.bootstrap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.solveria.ai.application.dto.EmployeeHandoffRecordDto;
import com.solveria.ai.application.dto.PayrollHandoffEventDto;
import com.solveria.ai.application.port.in.IngestAttendancePeriodUseCase;
import com.solveria.ai.application.port.out.VectorIngestionPort;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class IngestAttendancePeriodIntegrationTest {

    @Autowired private ApplicationEventPublisher eventPublisher;

    @Autowired private IngestAttendancePeriodUseCase useCase;

    @Autowired private VectorIngestionPort mockVectorIngestionPort;

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public VectorIngestionPort mockVectorIngestionPort() {
            return mock(VectorIngestionPort.class);
        }
    }

    @Test
    void testIngestAttendancePeriodFlow() throws InterruptedException {
        // Arrange
        EmployeeHandoffRecordDto record1 = new EmployeeHandoffRecordDto("emp-101", 3, 160.0, 10.0);
        EmployeeHandoffRecordDto record2 = new EmployeeHandoffRecordDto("emp-102", 0, 160.0, 5.0);

        PayrollHandoffEventDto event =
                new PayrollHandoffEventDto(
                        "tenant-xyz",
                        "org-unit-789",
                        "2026-05-01",
                        "2026-05-31",
                        List.of(record1, record2));

        // Act
        eventPublisher.publishEvent(event);

        // Wait brief moment for async execution
        Thread.sleep(500);

        // Assert
        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> tenantCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> namespaceCaptor = ArgumentCaptor.forClass(String.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> metadataCaptor = ArgumentCaptor.forClass(Map.class);

        verify(mockVectorIngestionPort, atLeastOnce())
                .persistTextChunk(
                        textCaptor.capture(),
                        tenantCaptor.capture(),
                        namespaceCaptor.capture(),
                        metadataCaptor.capture());

        List<String> capturedTexts = textCaptor.getAllValues();
        List<Map<String, Object>> capturedMetadata = metadataCaptor.getAllValues();

        assertEquals(2, capturedTexts.size());

        // Assertions for employee 1
        String text1 = capturedTexts.get(0);
        assertTrue(text1.contains("emp-101"));
        assertTrue(text1.contains("3 faltas injustificadas"));
        assertTrue(text1.contains("tenant-xyz"));

        Map<String, Object> meta1 = capturedMetadata.get(0);
        assertEquals("2026-05-01", meta1.get("periodStart"));
        assertEquals("2026-05-31", meta1.get("periodEnd"));
        assertEquals("org-unit-789", meta1.get("orgUnitId"));
        assertEquals("emp-101", meta1.get("relationshipId"));
        assertEquals(3, meta1.get("unjustifiedAbsences"));

        // Assertions for employee 2
        String text2 = capturedTexts.get(1);
        assertTrue(text2.contains("emp-102"));
        assertTrue(text2.contains("0 faltas injustificadas"));
        assertTrue(text2.contains("tenant-xyz"));
    }
}
