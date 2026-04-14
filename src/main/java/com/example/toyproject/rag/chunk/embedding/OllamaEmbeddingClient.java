package com.example.toyproject.rag.chunk.embedding;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 텍스트 문장을 받아서 임베딩처리(텍스트->수치화) 후 전달
 * Ollama 임베딩 클라이언트
 * - 로컬 Ollama 서버(기본 11434)에 요청해서 임베딩 벡터를 받아온다.
 * - 모델: nomic-embed-text (768차원)
 */
@Component
public class OllamaEmbeddingClient {
    // 문장을 수치화 시키는 함수
    private final WebClient webClient;

    public OllamaEmbeddingClient(WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl("http://localhost:11434") // ollama 로컬 주소, 포트 ollama 모델 연결
                .build();
    }

    /** 문장을 수치화로 만든 값 return  */
    public float[] embed768(String input) {
        if (input == null || input.isBlank()) throw new IllegalArgumentException("input is blank");

        // Ollama embeddings API
        // POST /api/embeddings
        // { "model": "nomic-embed-text"(임베딩 모델), "prompt": "..." }
        Map<String, Object> req = Map.of(
                "model", "nomic-embed-text",
                "prompt", input
        );

        OllamaEmbeddingResponse res = webClient.post()
                .uri("/api/embeddings")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .retrieve()
                .bodyToMono(OllamaEmbeddingResponse.class)
                .timeout(Duration.ofSeconds(20))
                .block();

        if (res == null || res.embedding == null || res.embedding.isEmpty()) {
            throw new IllegalStateException("Ollama embedding response is empty");
        }

        if (res.embedding.size() != 768) {
            throw new IllegalStateException("Embedding dims mismatch. expected=768 actual=" + res.embedding.size());
        }

        float[] out = new float[768];
        for (int i = 0; i < 768; i++) out[i] = res.embedding.get(i).floatValue();
        return out;
    }

    // ---- DTO ----
    static class OllamaEmbeddingResponse {
        public List<Double> embedding;
    }
}