package com.solveria.ai.bootstrap.config;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Explicit Gemini EmbeddingModel bean to satisfy PgVector autoconfiguration in dev.
 */

//@ConditionalOnProperty(prefix = "spring.ai.google.genai.embedding", name = "api-key")
public class GeminiEmbeddingConfig {

    @Bean
    @ConditionalOnMissingBean(EmbeddingModel.class)
    public EmbeddingModel geminiEmbeddingModel(
            @Value("${spring.ai.google.genai.embedding.api-key}") String apiKey,
            @Value("${spring.ai.google.genai.embedding.options.model:gemini-embedding-001}") String model,
            @Value("${spring.ai.google.genai.embedding.base-url:https://generativelanguage.googleapis.com/v1beta}")
                    String baseUrl) {
        RestClient restClient = RestClient.builder().baseUrl(baseUrl).build();
        return new GeminiEmbeddingModel(restClient, apiKey, model);
    }

    static final class GeminiEmbeddingModel implements EmbeddingModel {
        private static final Integer DimensionVector = 768;
        private final RestClient restClient;
        private final String apiKey;
        private final String model;

        private GeminiEmbeddingModel(RestClient restClient, String apiKey, String model) {
            this.restClient = restClient;
            this.apiKey = apiKey;
            this.model = model;
        }

        @Override
        public EmbeddingResponse call(EmbeddingRequest request) {
            List<String> inputs = extractInputs(request);
            if (inputs.isEmpty()) {
                return new EmbeddingResponse(List.of());
            }
            List<Embedding> embeddings = new ArrayList<>(inputs.size());
            for (int i = 0; i < inputs.size(); i++) {
                float[] vector = embedText(inputs.get(i));
                embeddings.add(new Embedding(vector, i));
            }
            return new EmbeddingResponse(embeddings);
        }

        @Override
        public float[] embed(Document document) {
            if (document == null || document.getText() == null) {
                return new float[0];
            }
            return embed(document.getText());
        }

        private float[] embedText(String text) {
            String modelPath = model.startsWith("models/") ? model : "models/" + model;
            GeminiEmbedRequest body =
                    new GeminiEmbedRequest(modelPath, new GeminiContent(List.of(new GeminiPart(text))), DimensionVector);
            GeminiEmbedResponse response =
                    restClient
                            .post()
                            .uri(
                                    uriBuilder ->
                                            uriBuilder
                                                    .path("/" + modelPath + ":embedContent")
                                                    .queryParam("key", apiKey)
                                                    .build())
                            .body(body)
                            .retrieve()
                            .body(GeminiEmbedResponse.class);
            if (response == null || response.embedding() == null || response.embedding().values() == null) {
                return new float[0];
            }
            List<Double> values = response.embedding().values();
            float[] vector = new float[values.size()];
            for (int i = 0; i < values.size(); i++) {
                vector[i] = values.get(i).floatValue();
            }
            return vector;
        }

        private static List<String> extractInputs(EmbeddingRequest request) {
            if (request == null) {
                return List.of();
            }
            List<String> inputs = coerceToStringList(invoke(request, "getInputs"));
            if (!inputs.isEmpty()) {
                return inputs;
            }
            inputs = coerceToStringList(invoke(request, "getInput"));
            if (!inputs.isEmpty()) {
                return inputs;
            }
            inputs = coerceToStringList(invoke(request, "getInstructions"));
            if (!inputs.isEmpty()) {
                return inputs;
            }
            return coerceToStringList(invoke(request, "getText"));
        }

        private static Object invoke(Object target, String methodName) {
            try {
                Method method = target.getClass().getMethod(methodName);
                return method.invoke(target);
            } catch (ReflectiveOperationException ignored) {
                return null;
            }
        }

        private static List<String> coerceToStringList(Object value) {
            if (value == null) {
                return List.of();
            }
            if (value instanceof String s) {
                return List.of(s);
            }
            if (value instanceof Document d) {
                return d.getText() == null ? List.of() : List.of(d.getText());
            }
            if (value instanceof List<?> list) {
                List<String> result = new ArrayList<>(list.size());
                for (Object item : list) {
                    if (item instanceof String s) {
                        result.add(s);
                    } else if (item instanceof Document d && d.getText() != null) {
                        result.add(d.getText());
                    }
                }
                return result;
            }
            return List.of();
        }
    }

    record GeminiEmbedRequest(String model, GeminiContent content, Integer outputDimensionality) {}

    record GeminiContent(List<GeminiPart> parts) {}

    record GeminiPart(String text) {}

    record GeminiEmbedResponse(GeminiEmbedding embedding) {}

    record GeminiEmbedding(List<Double> values) {}
}
