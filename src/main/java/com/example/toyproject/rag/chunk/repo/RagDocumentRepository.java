package com.example.toyproject.rag.chunk.repo;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class RagDocumentRepository {
    // RAG DB 연결을 위한 생성자 변수 선언
    private final JdbcTemplate ragJdbcTemplate;

    // DB 연결을 위해 DataSource 연결 고리 역할
    public RagDocumentRepository(@Qualifier("ragJdbcTemplate") JdbcTemplate ragJdbcTemplate) {
        this.ragJdbcTemplate = ragJdbcTemplate;
    }

    // 조회 결과 쿼리
    public Optional<String> findContentById(long documentId) {
        String sql = "select content from documents where id = ?";
        return ragJdbcTemplate.query(sql, rs -> {
            if (rs.next()) {
                return Optional.ofNullable(rs.getString("content"));
            }
            return Optional.empty();
        }, documentId);
    }

    /** Top-K 검색 (pgvector <=>) 최종 답변 생성 쿼리 */
    public List<TopChunkRow> searchTopKByEmbeddingLiteral(String embeddingLiteral, int topK) {
        // embeddingLiteral: "[0.1,0.2,...]" 형태 (아래 서비스에서 만들어서 넣음)
        String sql = """
            select
                id,
                document_id,
                chunk_index,
                content,
                (embedding_768 <=> (?::vector)) as distance
            from chunks
            where embedding_768 is not null
            order by embedding_768 <=> (?::vector)
            limit ?
        """;
        return ragJdbcTemplate.query(
                sql,
                (rs, rowNum) -> new TopChunkRow(
                        rs.getLong("id"),
                        rs.getLong("document_id"),
                        rs.getInt("chunk_index"),
                        rs.getString("content"),
                        rs.getDouble("distance")   // ✅ 여기 콤마 제거
                ),
                embeddingLiteral,
                embeddingLiteral,
                topK
        );
    }

    public record TopChunkRow(
            long chunkId,
            long documentId,
            int chunkIndex,
            String content,
            double distance
    ) {}
}
