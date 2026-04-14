package com.example.toyproject.rag.chunk.embedding;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import com.example.toyproject.rag.chunk.dto.OllamaGenerateResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Ollama Chat Client
 * - 로컬 Ollama(/api/chat) 호출해서 답변 텍스트를 받아온다.
 *
 * 주의:
 * - 모델명은 너 로컬에 pull되어 있는 모델로 맞춰야 함
 * - 기본값: llama3.1 (없으면 mistral 등으로 변경)
 */
@Component
public class OllamaChatClient {

    private final WebClient webClient;

    public OllamaChatClient(WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl("http://localhost:11434")
                .build();
    }

    //public String chat(String model, String system, String user) {
    // LLM이 실제로 답변 문장을 생성하는 역할
    public String generate(String model, String prompt){
        System.out.println("[OLLAMA] POST http://localhost:11434/api/generate model=" + model);

        // Ollama chat API (non-stream)
        // POST /api/chat
        Map<String, Object> req = Map.of(
                "model", model,
                "prompt", prompt,
                "stream", false
        );

        OllamaGenerateResponse res = webClient.post()
                //.uri("/api/chat")
                .uri("/api/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .retrieve()
                .bodyToMono(OllamaGenerateResponse.class)
                .timeout(Duration.ofSeconds(60))
                .block();

        /*class → res.response
        record → res.response()*/

        if (res == null || res.response() == null) {
            throw new IllegalStateException("Ollama generate response is empty");
        }
        return res.response();
    }

    // ---- DTO ----
    static class OllamaChatResponse {
        public Message message;
        static class Message {
            public String role;
            public String content;
        }
    }
}
