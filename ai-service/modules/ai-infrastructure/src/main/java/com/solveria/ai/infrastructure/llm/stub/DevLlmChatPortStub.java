package com.solveria.ai.infrastructure.llm.stub;

import com.solveria.ai.application.dto.ChatResultDto;
import com.solveria.ai.application.port.out.LlmChatPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Stub LlmChatPort implementation for dev and test profiles. Only activated when no real
 * LlmChatPort bean (e.g. SpringAiLlmChatAdapter) is present in the context.
 */
@Component
@ConditionalOnMissingBean(LlmChatPort.class)
@Profile({"dev", "test"})
public class DevLlmChatPortStub implements LlmChatPort {

    private static final Logger log = LoggerFactory.getLogger(DevLlmChatPortStub.class);

    public DevLlmChatPortStub() {
        log.info("DevLlmChatPortStub initialized - using stub LLM responses in dev/test profile");
    }

    @Override
    public ChatResultDto chat(String prompt) {
        log.debug("DevLlmChatPortStub.chat() called with prompt: {}", prompt);
        return new ChatResultDto("[DEV-STUB] LLM no configurado. Respuesta simulada.", 0, 0);
    }
}
