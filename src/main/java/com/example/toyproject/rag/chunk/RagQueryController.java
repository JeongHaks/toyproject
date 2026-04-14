package com.example.toyproject.rag.chunk;

import com.example.toyproject.rag.chunk.domain.Chunk;
import com.example.toyproject.rag.chunk.dto.RagQueryRequest;
import com.example.toyproject.rag.chunk.dto.RagQueryResponse;
import com.example.toyproject.rag.chunk.service.RagDocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 질문 API (RAG)
 *
 * 책임:
 * - question을 받아서
 * - Top-K 검색 수행 (근거 청크)
 * - LLM 답변 생성 (answer)
 * - JSON으로 반환: { answer, results }
 */
@RestController
@RequestMapping("/rag/query")
public class RagQueryController {
        // 채팅창에 내용 입력
    private final RagDocumentService ragDocumentService;

    public RagQueryController(RagDocumentService ragDocumentService) {
        this.ragDocumentService = ragDocumentService;
    }

    @PostMapping
    public ResponseEntity<RagQueryResponse> query(@RequestBody RagQueryRequest req) {
        System.out.println("채팅 답변 11111111111111111111111111111");

        // 질문과 유사근거 값 전달 받기.
        String question = (req.getQuestion() == null) ? "" : req.getQuestion().trim();
        int topK = (req.getTopK() == null) ? 3 : req.getTopK();

        // 질문이 비어져있으면.
        if (question.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        // 질문에 대한 유사 근거 답변 최소 3개~10개
        if (topK <= 0) topK = 3;
        if (topK > 10) topK = 10;

        //  1) Top-K 검색 (근거)
        List<Chunk> chunks = ragDocumentService.searchTopK(question, topK);
        System.out.println("여기로 오시나요 생성형 AI 님");
        //  2) 답변 생성 (LLM)
        // - 내부에서 다시 Top-K를 호출하지만, 지금 단계는 데모 우선이라 단순하게 간다.
        // - 최적화는 다음 단계에서 "chunks를 answer로 넘기기"로 개선 가능.
        String answer = ragDocumentService.answer(question, topK);

        // results: preview만 내려서 payload 제한
        List<RagQueryResponse.ChunkResult> results = chunks.stream()
                .map(c -> new RagQueryResponse.ChunkResult(
                        c.getId(),
                        c.getDocumentId(),
                        c.getChunkIndex(),
                        preview(c.getContent(), 400)
                ))
                .toList();

        // 최종 응답: answer + results
        return ResponseEntity.ok(new RagQueryResponse(answer, results));
    }

    private static String preview(String text, int maxLen) {
        if (text == null) return "";
        String t = text.replace("\r", " ")
                .replace("\n", " ")
                .trim();
        if (t.length() <= maxLen) return t;
        return t.substring(0, maxLen) + "...";
    }
}
