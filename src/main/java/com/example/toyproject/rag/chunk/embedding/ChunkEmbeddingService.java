package com.example.toyproject.rag.chunk.embedding;

import java.sql.SQLException;
import java.util.Optional;

import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChunkEmbeddingService {

    private final JdbcTemplate ragJdbcTemplate;
    private final OllamaEmbeddingClient embeddingClient;

    public ChunkEmbeddingService(@Qualifier("ragJdbcTemplate") JdbcTemplate ragJdbcTemplate,
                                 OllamaEmbeddingClient embeddingClient) {
        this.ragJdbcTemplate = ragJdbcTemplate;
        this.embeddingClient = embeddingClient;
    }

    /**
     * documentId에 속하면서 embedding_768이 NULL인 chunk 1개를 찾아 embedding을 생성/저장
     */
    @Transactional(transactionManager = "ragTransactionManager")
    public EmbedOneResult embedOneChunk(long documentId) {

        Optional<ChunkRow> target = findOneChunkWithoutEmbedding(documentId);
        if (target.isEmpty()) {
            return new EmbedOneResult(false, null,
                    "No chunk without embedding_768 for documentId=" + documentId);
        }

        ChunkRow row = target.get();

        // 1) Ollama 임베딩 생성 (768차원)
        float[] vec = embeddingClient.embed768(row.getContent());

        // (선택) 차원 안전장치
        if (vec == null || vec.length != 768) {
            return new EmbedOneResult(false, row.getId(),
                    "Embedding dims invalid. dims=" + (vec == null ? "null" : vec.length));
        }

        // 2) pgvector(vector)로 저장
        PGobject pgVector = toPgVector(vec);

        int updated = ragJdbcTemplate.update(
                "UPDATE chunks SET embedding_768 = ? WHERE id = ?",
                pgVector, row.getId()
        );

        if (updated != 1) {
            return new EmbedOneResult(false, row.getId(), "Update failed. updated=" + updated);
        }

        return new EmbedOneResult(true, row.getId(), "Saved embedding dims=" + vec.length);
    }

    /**
     * (A) 문서 단위 배치 임베딩
     * - embedding_768 IS NULL 인 chunk만 전부 처리
     * - 기존 embedOneChunk(documentId) 재사용 (구조 변경 최소)
     */
    public EmbedAllResult embedAllChunks(long documentId) {
        int processed = 0;

        // 안전장치: 무한루프 방지
        final int maxLoop = 10_000;

        for (int i = 0; i < maxLoop; i++) {
            EmbedOneResult r = embedOneChunk(documentId);

            // 더 이상 처리할 게 없으면 정상 종료 (혹은 실패 종료)
            if (!r.isSuccess()) {
                // "없어서 종료"는 정상일 수 있으니 success=true로 반환
                // (원하면 메시지로 구분)
                String msg = "Done. processed=" + processed + " | last=" + r.getMessage();
                return new EmbedAllResult(true, processed, msg);
            }

            processed++;
        }

        return new EmbedAllResult(false, processed,
                "Stopped by maxLoop=" + maxLoop + " (prevent infinite loop). processed=" + processed);
    }

    private Optional<ChunkRow> findOneChunkWithoutEmbedding(long documentId) {
        return ragJdbcTemplate.query(
                """
                SELECT id, content
                FROM chunks
                WHERE document_id = ?
                  AND embedding_768 IS NULL
                ORDER BY chunk_index ASC
                LIMIT 1
                """,
                rs -> {
                    if (!rs.next()) return Optional.empty();
                    return Optional.of(new ChunkRow(
                            rs.getLong("id"),
                            rs.getString("content")
                    ));
                },
                documentId
        );
    }

    private PGobject toPgVector(float[] vec) {
        // pgvector 입력 포맷: [0.1,0.2,0.3,...]
        StringBuilder sb = new StringBuilder(vec.length * 10);
        sb.append('[');
        for (int i = 0; i < vec.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(vec[i]);
        }
        sb.append(']');

        PGobject pg = new PGobject();
        pg.setType("vector");
        try {
            pg.setValue(sb.toString());
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to build pgvector value", e);
        }
        return pg;
    }

    // ===== record 대신 일반 클래스/DTO로 변경 (컴파일 호환성 최대) =====

    private static class ChunkRow {
        private final long id;
        private final String content;

        private ChunkRow(long id, String content) {
            this.id = id;
            this.content = content;
        }

        public long getId() { return id; }
        public String getContent() { return content; }
    }

    public static class EmbedOneResult {
        private final boolean success;
        private final Long chunkId;
        private final String message;

        public EmbedOneResult(boolean success, Long chunkId, String message) {
            this.success = success;
            this.chunkId = chunkId;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public Long getChunkId() { return chunkId; }
        public String getMessage() { return message; }
    }

    public static class EmbedAllResult {
        private final boolean success;
        private final int processed;
        private final String message;

        public EmbedAllResult(boolean success, int processed, String message) {
            this.success = success;
            this.processed = processed;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public int getProcessed() { return processed; }
        public String getMessage() { return message; }
    }
}
