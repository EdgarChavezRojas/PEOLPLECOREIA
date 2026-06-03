package com.solveria.ai.infrastructure.llm.springai;

import com.solveria.ai.application.dto.ChatResultDto;
import com.solveria.ai.application.port.out.LlmChatPort;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * LLM chat adapter via Spring AI ChatClient. Returns answer + usage. Only created when OpenAI API
 * key is configured and ChatClient.Builder is available.
 */
@Component
@Primary
//@ConditionalOnProperty(prefix = "spring.ai.google.genai", name = "api-key")
public class SpringAiLlmChatAdapter implements LlmChatPort {

    private final ChatModel chatModel;

    public SpringAiLlmChatAdapter(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public ChatResultDto chat(String prompt) {
        try {
            // Llamada real al modelo de Google Gemini a través de internet
            System.out.println("\n=== ENVIANDO ESTE PROMPT REAL A GEMINI ===\n" + prompt + "\n=========================================\n");
            ChatResponse response = chatModel.call(new Prompt(prompt));

            String answer = response.getResult().getOutput().getText();

            // Intentar extraer los tokens reales si el proveedor los expone en la metadata
            int promptTokens = response.getMetadata().getUsage() != null ?
                    (int) response.getMetadata().getUsage().getPromptTokens() : 100;
            int completionTokens = response.getMetadata().getUsage() != null ?
                    (int) response.getMetadata().getUsage().getTotalTokens() : 100;

            return new ChatResultDto(answer, promptTokens, completionTokens);
        } catch (Exception e) {
            // Salvaguarda en caso de caída de conexión de red para que veas el error real en tu consola
            return new ChatResultDto("[ERROR REAL GEMINI] Falló la llamada HTTP: " + e.getMessage(), 0, 0);
        }
    }
}
