package com.solveria.ai.application.port.out;

import java.util.Map;

public interface VectorIngestionPort {
    void persistTextChunk(
            String text, String tenantId, String namespace, Map<String, Object> metadata);
}
