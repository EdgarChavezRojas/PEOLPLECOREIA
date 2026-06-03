package com.solveria.ai.bootstrap.config;

import com.solveria.ai.application.port.in.CompletePromptUseCase;
import com.solveria.ai.application.port.in.RagQaUseCase;
import com.solveria.ai.application.port.out.*;
import com.solveria.ai.application.service.CompletePromptService;
import com.solveria.ai.application.service.RagQaService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class UseCaseConfig {

    @Bean
    public CompletePromptUseCase completePromptUseCase(LlmPort llmPort) {
        return new CompletePromptService(llmPort);
    }

    @Bean
    public RagQaUseCase ragQaUseCase(
            TenantContextPort tenantContext,
            VectorStorePort vectorStore,
            LlmChatPort llmChat,
            AuditPort audit) {
        return new RagQaService(tenantContext, vectorStore, llmChat, audit);
    }

    @Bean
    public com.solveria.ai.application.port.in.IngestAttendancePeriodUseCase
            ingestAttendancePeriodUseCase(
                    com.solveria.ai.application.port.out.VectorIngestionPort vectorIngestionPort) {
        return new com.solveria.ai.application.service.IngestAttendancePeriodService(
                vectorIngestionPort);
    }
}
