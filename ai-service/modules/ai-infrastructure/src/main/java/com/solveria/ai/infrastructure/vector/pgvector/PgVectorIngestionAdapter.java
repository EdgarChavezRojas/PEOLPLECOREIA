package com.solveria.ai.infrastructure.vector.pgvector;

import com.solveria.ai.application.port.out.VectorIngestionPort;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
//@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
//        prefix = "spring.ai.google.genai",
//        name = "api-key"
//)
public class PgVectorIngestionAdapter implements VectorIngestionPort {

    private final VectorStore vectorStore;

    public PgVectorIngestionAdapter(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public void persistTextChunk(
            String text, String tenantId, String namespace, Map<String, Object> metadata) {
        Map<String, Object> docMetadata =
                metadata != null ? new HashMap<>(metadata) : new HashMap<>();
        docMetadata.put("tenantId", tenantId);
        docMetadata.put("namespace", namespace);

        Document document = new Document(text, docMetadata);
        vectorStore.add(List.of(document));
    }
}
