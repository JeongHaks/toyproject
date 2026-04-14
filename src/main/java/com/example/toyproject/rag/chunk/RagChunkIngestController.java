package com.example.toyproject.rag.chunk;


import com.example.toyproject.rag.chunk.service.RagChunkIngestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * RAG 문서 -> Chunk 분할 -> chunks 테이블 저장(ingest) 실행용 컨트롤러
 *
 * 목적
 * - "documentId 하나"를 지정해서
 * - 해당 documents.content를 ChunkSplitter로 자른 뒤
 * - chunks 테이블에 저장한다.
 *
 * 주의
 * - 임베딩 생성은 아직 안 한다. (다음 단계)
 * - token_count도 아직 저장 안 한다. (다음 단계)
 */
@RestController
@RequestMapping("/rag")
@RequiredArgsConstructor
public class RagChunkIngestController {

    private final RagChunkIngestService ragChunkIngestService;

    /**
     * chunks 저장 실행 API
     *
     * 사용 예)
     * POST /rag/ingest/1?chunkSize=800&overlap=150
     *
     * @param documentId documents 테이블의 PK id
     * @param chunkSize  한 chunk 최대 길이(기본 800)
     * @param overlap    chunk 간 겹치는 길이(기본 150)
     *
     * @return 저장된 chunk 개수 등 요약 정보(JSON)
     */
    @PostMapping("/ingest/{documentId}")
    public Map<String, Object> ingest(
            @PathVariable long documentId,
            @RequestParam(defaultValue = "800") int chunkSize,
            @RequestParam(defaultValue = "150") int overlap
    ) {
        int savedCount = ragChunkIngestService.ingestChunks(documentId, chunkSize, overlap);

        return Map.of(
                "documentId", documentId,
                "chunkSize", chunkSize,
                "overlap", overlap,
                "savedChunkCount", savedCount
        );
    }
}