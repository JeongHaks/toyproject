package com.example.toyproject.rag.chunk.embedding;


import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.List;

@Component
public class OpenAiEmbeddingClient {

    private final WebClient webClient;

    public OpenAiEmbeddingClient(
            WebClient.Builder builder,
            @Value("${openai.api-key}") String apiKey
    ) {
        this.webClient = builder
                .baseUrl("https://api.openai.com")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /** @return 1536-dim embedding */
    public float[] embed1536(String input) {
        if (input == null || input.isBlank()) throw new IllegalArgumentException("input is blank");

        EmbeddingsRequest req = new EmbeddingsRequest(
                "text-embedding-3-small",
                input,
                "float",
                1536
        );

        try {
            EmbeddingsResponse res = webClient.post()
                    .uri("/v1/embeddings")
                    .bodyValue(req)
                    .retrieve()
                    .bodyToMono(EmbeddingsResponse.class)
                    .timeout(Duration.ofSeconds(20))
                    .block();

            if (res == null || res.data == null || res.data.isEmpty()
                    || res.data.get(0).embedding == null || res.data.get(0).embedding.isEmpty()) {
                throw new IllegalStateException("OpenAI embeddings response is empty");
            }

            List<Double> vec = res.data.get(0).embedding;
            if (vec.size() != 1536) {
                throw new IllegalStateException("Embedding dims mismatch. expected=1536 actual=" + vec.size());
            }

            float[] out = new float[1536];
            for (int i = 0; i < 1536; i++) out[i] = vec.get(i).floatValue();
            return out;

        } catch (WebClientResponseException e) {
            throw new IllegalStateException(
                    "OpenAI embeddings API error: " + e.getStatusCode() + " body=" + e.getResponseBodyAsString(),
                    e
            );
        }
    }

    // ---- DTOs ----
    static class EmbeddingsRequest {
        public String model;
        public Object input;
        public String encoding_format;
        public Integer dimensions;

        EmbeddingsRequest(String model, Object input, String encoding_format, Integer dimensions) {
            this.model = model;
            this.input = input;
            this.encoding_format = encoding_format;
            this.dimensions = dimensions;
        }
    }

    static class EmbeddingsResponse {
        public List<EmbeddingData> data;
    }

    static class EmbeddingData {
        public List<Double> embedding;
    }
}