package com.example.toyproject.rag.chunk;

import com.example.toyproject.rag.chunk.service.RagChunkTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/rag/test")
@RequiredArgsConstructor
public class RagChunkTestController {

    private final RagChunkTestService ragChunkTestService;

    /**
     * documents 테이블의 content를 chunk로 분할한 결과 확인용
     * - 분할된 chunk 개수
     * - 분할된 각 chunk 길이
     */
    @GetMapping("/chunks/{documentId}")
    public Map<String, Object> testChunkSplit(
            @PathVariable(name="documentId") Long documentId,
            @RequestParam(defaultValue = "800") int chunkSize,
            @RequestParam(defaultValue="150") int overlap) {
        // chunk 분할 로직으로 이동
        return ragChunkTestService.testChunkSplit(documentId,chunkSize, overlap);
    }
}
