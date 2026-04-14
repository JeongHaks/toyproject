package com.example.toyproject.rag.chunk;


import com.example.toyproject.rag.chunk.service.RagDocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

// RAG 문서 업로드 Web(Controller)
// 파일 (PDF 등)을 HTTP 로 받는다.
// 실제 로직은 비즈니스 로직 Service에서 작성
@RestController
@RequestMapping("/rag/document")
public class RagDocumentController {
    private final RagDocumentService ragDocumentService;

    // 생성자 기반 의존성 주입
    // RagDocumentController는 RagDocumentService 없으면 동작 안 함.
    public RagDocumentController(RagDocumentService ragDocumentService) {
        this.ragDocumentService = ragDocumentService;
    }

    /**
     * 문서 업로드 API
     *
     * 현재 단계에서는:
     * - 파일을 받는다
     * - documents 테이블에 문서 1건만 생성한다
     * - 인덱싱/청킹/임베딩은 "아직 안 함"
     */
    @PostMapping
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        System.out.println("branch 분리 후 commit Test!!!!!!!!!!!!!");
        Long documentId = ragDocumentService.createDocument(file);

        return ResponseEntity.ok(
                Map.of(
                        "documentId", documentId,
                        "status", "UPLOADED"
                )
        );
    }
}
