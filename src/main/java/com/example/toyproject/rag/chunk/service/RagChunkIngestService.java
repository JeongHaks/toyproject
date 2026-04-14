package com.example.toyproject.rag.chunk.service;

import com.example.toyproject.rag.chunk.ChunkSqlitter;
import com.example.toyproject.rag.chunk.repo.RagChunkRepository;
import com.example.toyproject.rag.chunk.repo.RagDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RagChunkIngestService {

    private final RagDocumentRepository ragDocumentRepository;
    private final RagChunkRepository ragChunkRepository;

    @Qualifier("ragDataSource")
    private final DataSource ragDataSource;

    /**
     * documents.content -> split -> chunks insert
     * (임베딩/토큰카운트는 아직 안 함)
     */
    //반드시 RAG DB 트랜잭션 매니저를 타야한다.
    @Transactional("ragTransactionManager")
    public int ingestChunks(long documentId, int chunkSize, int overlap) {
        System.out.println("### ingestChunks CALLED documentId=" + documentId);

        try {
            String url = ragDataSource.getConnection().getMetaData().getURL();
            log.info("[RAG] DB URL={}", url);
        } catch (Exception e) {
            log.warn("[RAG] DB URL 확인 실패", e);
        }

        log.info("[RAG] ingestChunks called. documentId={}", documentId);

        // 문서 ID 확인
        String content = ragDocumentRepository.findContentById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("document not found: " + documentId));

        // 문장 내용이 없거나, 빈 공간일 경우
        if (content == null || content.isBlank()) {
            throw new IllegalStateException("document content is empty: " + documentId);
        }

        // 1) split(chunk 문장쪼갬)
        List<String> chunks = ChunkSqlitter.splitToChunks(content, chunkSize, overlap);

        // 2) 기존 chunk 삭제(재실행 안전)재실행해도 중복 안 쌓이게
        ragChunkRepository.deleteByDocumentId(documentId);

        // 3) insert(쪼갬 단위로 DB에 저장) 2문장으로 쪼갤경우 2개가 저장될것.
        for (int i = 0; i < chunks.size(); i++) {
            ragChunkRepository.insertChunk(documentId, i, chunks.get(i));
        }

        return chunks.size();
    }
}