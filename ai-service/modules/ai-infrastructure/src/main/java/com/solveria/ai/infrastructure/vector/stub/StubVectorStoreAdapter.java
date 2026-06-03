package com.solveria.ai.infrastructure.vector.stub;

import com.solveria.ai.application.dto.RagChunkDto;
import com.solveria.ai.application.port.out.VectorStorePort;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Stub VectorStorePort for dev and test profiles (when PgVector autoconfig is excluded). Returns
 * empty results for development and testing without requiring embeddings.
 */

@Profile({"dev", "test"})
@ConditionalOnMissingBean(VectorStore.class)
public class StubVectorStoreAdapter implements VectorStorePort {

    @Override
    public List<RagChunkDto> similaritySearch(
            String query, int topK, String tenantId, String namespace) {
        return List.of();
    }
}
