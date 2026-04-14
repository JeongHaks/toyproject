package com.example.toyproject.rag.chunk.service;

import com.example.toyproject.rag.chunk.dto.RagAnswerResult;
import com.example.toyproject.rag.chunk.repo.RagDocumentRepository;
import com.example.toyproject.rag.chunk.repo.RagDocumentRepository.TopChunkRow;
import com.example.toyproject.rag.chunk.ChunkSqlitter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RagChunkTestService {

    private final RagDocumentRepository ragDocumentRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    @Value("${ollama.embed-model:nomic-embed-text}")
    private String embedModel;

    @Value("${ollama.chat-model:gemma:2b}")
    private String chatModel;

    public RagChunkTestService(RagDocumentRepository ragDocumentRepository, ObjectMapper objectMapper) {
        this.ragDocumentRepository = ragDocumentRepository;
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate();
    }

    /** 기존 로직 (유지) */
    public List<String> splitDocument(long documentId, int chunkSize, int overlap) {
        String content = ragDocumentRepository.findContentById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("document not found: " + documentId));

        return ChunkSqlitter.splitToChunks(content, chunkSize, overlap);
    }

    /** 테스트 API 전용(청크 분할 결과) */
    public Map<String, Object> testChunkSplit(long documentId, int chunkSize, int overlap) {

        String content = ragDocumentRepository.findContentById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("document not found: " + documentId));

        if (content.isBlank()) {
            throw new IllegalStateException("document content is empty: " + documentId);
        }

        List<String> chunks = ChunkSqlitter.splitToChunks(content, chunkSize, overlap);

        List<Integer> chunkLengths = chunks.stream()
                .map(String::length)
                .toList();

        Map<String, Object> result = new HashMap<>();
        result.put("documentId", documentId);
        result.put("originalLength", content.length());
        result.put("chunkCount", chunks.size());
        result.put("chunkLengths", chunkLengths);

        return result;
    }

    /**
     * 최종: 질문 → 임베딩 → Top-K → 답변 생성 → (answer/results/sources_json/distance_top1) 반환
     */
    public RagAnswerResult answer(String question, int topK) {

        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("question is blank");
        }
        if (topK <= 0) topK = 5;

        // 1) 질문 임베딩(Ollama)
        float[] qEmbedding = embed(question);
        String embeddingLiteral = toVectorLiteral(qEmbedding); // "[...]" 문자열

        // 2) Top-K 검색(pgvector <=>)
        List<TopChunkRow> top = ragDocumentRepository.searchTopKByEmbeddingLiteral(embeddingLiteral, topK);

        if (top.isEmpty()) {
            String fallback = "관련 문서를 찾지 못했어요. 다른 질문으로 시도해 주세요.";
            return new RagAnswerResult(fallback, List.of(), "[]", null);
        }

        // distance_top1
        Double distanceTop1 = top.get(0).distance();
        boolean weakContext = distanceTop1 != null && distanceTop1 > 0.6;


        // 3) sources_json 만들기 (DB 저장용)
        String sourcesJson = buildSourcesJson(top);

        // 4) 답변 생성(Ollama generate)
        String prompt = buildPrompt(question, top);
        String answer = generate(prompt);

        // 5) UI 근거 토글용 results (일단 TopChunkRow 그대로 내려도 OK)
        // UI에서 chunk.content 전체가 부담이면 contentPreview로 바꿔도 됨.
        List<Map<String, Object>> results = top.stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("chunkId", r.chunkId());
            m.put("documentId", r.documentId());
            m.put("chunkIndex", r.chunkIndex());
            m.put("distance", r.distance());
            m.put("content", r.content());
            return m;
        }).toList();

        return new RagAnswerResult(answer, results, sourcesJson, distanceTop1);
    }

    /* ---------------------------
       Ollama: embeddings
    ---------------------------- */
    private float[] embed(String text) {
        String url = ollamaBaseUrl + "/api/embeddings";

        Map<String, Object> body = new HashMap<>();
        body.put("model", embedModel);
        body.put("prompt", text);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> res = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                String.class
        );

        // 임베딩 성공/실패 여부
        if (!res.getStatusCode().is2xxSuccessful() || res.getBody() == null) {
            throw new IllegalStateException("ollama embeddings failed: " + res.getStatusCode());
        }

        try {
            JsonNode root = objectMapper.readTree(res.getBody());
            JsonNode arr = root.get("embedding");
            if (arr == null || !arr.isArray()) {
                throw new IllegalStateException("ollama embeddings response has no embedding array");
            }
            float[] out = new float[arr.size()];
            for (int i = 0; i < arr.size(); i++) {
                out[i] = (float) arr.get(i).asDouble();
            }
            return out;
        } catch (Exception e) {
            throw new IllegalStateException("ollama embeddings parse error", e);
        }
    }

    /* ---------------------------
       Ollama: generate(사용자 질문에 대한 답변 생성 메서드)
    ---------------------------- */
    private String generate(String prompt) {
        String url = ollamaBaseUrl + "/api/generate";

        Map<String, Object> body = new HashMap<>();
        body.put("model", chatModel);
        body.put("prompt", prompt);
        body.put("stream", false);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> res = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                String.class
        );
        // 답변 생성 실패시
        if (!res.getStatusCode().is2xxSuccessful() || res.getBody() == null) {
            throw new IllegalStateException("ollama generate failed: " + res.getStatusCode());
        }
        // 답변 생성
        try {
            JsonNode root = objectMapper.readTree(res.getBody());
            JsonNode ans = root.get("response");
            return (ans == null) ? "" : ans.asText();
        } catch (Exception e) {
            throw new IllegalStateException("ollama generate parse error", e);
        }
    }

    /* ---------------------------
       Prompt 구성 - 청크를 기반으로 LLM에게 전달할 프롬프트 생성
    ---------------------------- */
    private String buildPrompt(String question, List<TopChunkRow> top) {
        // DB에서 가져온 유사 청크들을 하나로 합치는 변수 ( documentId() : 어떤 문서인지, chunkIndex() : 몇 번째 청크인지, distance() : 유사도(0.123) )
        String context = top.stream().map(r -> "- [doc=" + r.documentId() + ", chunk=" + r.chunkIndex() + ", dist=" + r.distance() + "]\n" + r.content())
                .collect(Collectors.joining("\n\n"));

        return """
                너는 RAG 기반 어시스턴트다.
                
                규칙:
                1) 아래 [문서 근거]에 질문에 대한 '직접적인 답'이 있으면 그 근거를 바탕으로 답하라.
                2) [문서 근거]에 직접 답이 없으면, 일반 지식/추론으로 짧게 답하되,
                   답변 첫 줄에 반드시 "※ 문서에 명시적 근거 없음"을 붙여라.
                3) 문서 내용을 지어내지 마라.
                
                [질문]
                %s
                
                [문서 근거]
                %s
                """.formatted(question, context);
    }

    /* ---------------------------
       sources_json 생성 (DB 저장용) - 답변 근거 결과값을 저장
    ---------------------------- */
    private String buildSourcesJson(List<TopChunkRow> top) {
        try {
            List<Map<String, Object>> sources = top.stream().map(r -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("chunkId", r.chunkId());
                m.put("documentId", r.documentId());
                m.put("chunkIndex", r.chunkIndex());
                m.put("distance", r.distance());
                m.put("preview", preview(r.content(), 200));
                return m;
            }).toList();

            return objectMapper.writeValueAsString(sources);
        } catch (Exception e) {
            // 최악에도 저장은 되게
            return "[]";
        }
    }

    // 청크 원본을 그대로 저장하지 않고, 공백을 정리한 뒤 일부만 잘라서 미리보기 형태로 해주는 메서드
    private String preview(String s, int max) {
        if (s == null) return "";
        String t = s.replaceAll("\\s+", " ").trim();
        return (t.length() <= max) ? t : t.substring(0, max) + "...";
    }

    /* ---------------------------
       vector literal 변환: "[0.1,0.2,...]"
       - SQL에서 (?::vector)로 캐스팅
       - 임베딩 벡터 float[]를 pgvector가 처리할 수 있는 문자열 형태([0.123,0.456,...])로 변환하는 메서드.
       - 즉, 질문/문서의 벡터값을 DB 쿼리에서 사용하거나 저장할 수 있게 포맷을 변횐
    ---------------------------- */
    private String toVectorLiteral(float[] v) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < v.length; i++) {
            if (i > 0) sb.append(",");
            // 너무 긴 소수는 DB 파싱/용량에 불리 → 적당히 컷
            sb.append(String.format(Locale.US, "%.6f", v[i]));
        }
        sb.append("]");
        return sb.toString();
    }
}
