package com.solveria.ai.application.service;

import com.solveria.ai.application.dto.ChatResultDto;
import com.solveria.ai.application.dto.RagChunkDto;
import com.solveria.ai.application.dto.RagQaCommandDto;
import com.solveria.ai.application.dto.RagQaResultDto;
import com.solveria.ai.application.port.in.RagQaUseCase;
import com.solveria.ai.application.port.out.AuditPort;
import com.solveria.ai.application.port.out.LlmChatPort;
import com.solveria.ai.application.port.out.TenantContextPort;
import com.solveria.ai.application.port.out.VectorStorePort;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * RAG QA use case implementation. a) tenantId from TenantContextPort b) vector search topK with
 * mandatory tenantId + namespace filter c) build prompt with retrieved context d) call LlmChatPort,
 * return answer + usage
 */
public class RagQaService implements RagQaUseCase {

    private static final int DEFAULT_TOP_K = 10;
    private static final String RAG_SYSTEM_TEMPLATE =
            """
        Answer the question based only on the following context. If the context does not contain \
        relevant information, say so. Do not invent facts.
        The provided context contains consolidated attendance and payroll handover reports that can span multiple distinct historical months or periods (identifiable by dates, period limits, or yearMonth metadata).
        If the user requests a comparison, analysis of variance, or historical correlation between different months, periods, or dates:
        1. Identify and isolate the attendance records, absences (NO_SHOW), and relationship IDs corresponding to each historical period separately.
        2. Perform an exact mathematical calculation of the increase, decrease, or stability of unjustified absences between the periods.
        3. Draft a precise, executive-level comparison report. Contrast one period directly against the other, highlighting whether absenteeism rose or fell, and state the nominal or percentage variance.
        If the context completely lacks data for one of the requested historical periods, explicitly inform the user about the missing timeframe and do not invent figures.

        Context:
        %s

        Question: %s
        """;

    private final TenantContextPort tenantContext;
    private final VectorStorePort vectorStore;
    private final LlmChatPort llmChat;
    private final AuditPort audit;

    public RagQaService(
            TenantContextPort tenantContext,
            VectorStorePort vectorStore,
            LlmChatPort llmChat,
            AuditPort audit) {
        this.tenantContext = tenantContext;
        this.vectorStore = vectorStore;
        this.llmChat = llmChat;
        this.audit = audit;
    }

    @Override
    public RagQaResultDto ask(RagQaCommandDto command) {
        String tenantId = tenantContext.currentTenantId();
        List<RagChunkDto> chunks =
                vectorStore.similaritySearch(
                        command.question(), DEFAULT_TOP_K, tenantId, command.namespace());
        String context =
                chunks.stream().map(RagChunkDto::content).collect(Collectors.joining("\n\n"));
        if (context.isBlank()) {
            context = "(No relevant context found.)";
        }
        String prompt = RAG_SYSTEM_TEMPLATE.formatted(context, command.question());

        ChatResultDto chat = llmChat.chat(prompt);
        audit.audit(
                "rag.qa",
                Map.of(
                        "tenantId", tenantId,
                        "namespace", command.namespace(),
                        "promptTokens", chat.promptTokens(),
                        "completionTokens", chat.completionTokens()));
        return new RagQaResultDto(chat.answer(), chat.promptTokens(), chat.completionTokens());
    }
}
