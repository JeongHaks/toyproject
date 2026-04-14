package com.example.toyproject.rag.chunk.repo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
/**
 * RAG DB의 chunks 테이블에 접근하는 Repository (JdbcTemplate 버전)
 *
 * 하는 일
 * 1) 특정 document_id의 기존 chunks 삭제 (중복 방지, 재실행 안전)
 * 2) document_id + chunk_index + content 를 chunks 테이블에 INSERT
 *
 * 주의 사항
 * - 반드시 "RAG DB용 JdbcTemplate"을 주입받아야 함
 *   (main DB가 아니라 rag.datasource 쪽 DB)
 */
@Repository
public class RagChunkRepository {

    /**
     * RAG DB용 JdbcTemplate
     * - Config에서 ragJdbcTemplate 같은 Bean 이름으로 등록되어 있다고 가정
     * - 만약 Bean 이름이 다르면 @Qualifier 값을 너 설정에 맞게 바꿔야 함
     */
    private final JdbcTemplate ragJdbcTemplate;

    public RagChunkRepository(@Qualifier("ragJdbcTemplate") JdbcTemplate ragJdbcTemplate) {
        this.ragJdbcTemplate = ragJdbcTemplate;
    }

    /**
     * 같은 문서를 여러 번 ingest(재처리)할 때
     * 기존 chunks가 남아있으면 중복이 쌓이기 때문에 먼저 삭제한다.
     *
     * @param documentId documents 테이블의 id
     */
    public void deleteByDocumentId(long documentId) {
        String sql = "DELETE FROM chunks WHERE document_id = ?";
        ragJdbcTemplate.update(sql, documentId);
    }

    /**
     * chunks 테이블에 chunk 1개 row 저장
     *
     * @param documentId  원본 문서 ID
     * @param chunkIndex  0부터 시작하는 청크 순서
     * @param content     청크 텍스트(원문에서 잘라낸 내용)
     *
     * token_count / embedding 은 이 단계에서는 아직 저장하지 않는다.
     * (다음 단계에서 토큰 계산 / Embedding API 호출 후 UPDATE 할 예정)
     */
    public void insertChunk(long documentId, int chunkIndex, String content) {

        String sql = """
            INSERT INTO chunks (document_id, chunk_index, content, token_count, embedding, created_at)
            VALUES (?, ?, ?, NULL, NULL, NOW())
            """;

        ragJdbcTemplate.update(sql, documentId, chunkIndex, content);
    }

    /**
     * (선택) 디버깅/검증용:
     * 특정 document_id에 저장된 chunk row 개수 확인
     */
    public int countByDocumentId(long documentId) {
        String sql = "SELECT COUNT(*) FROM chunks WHERE document_id = ?";
        Integer count = ragJdbcTemplate.queryForObject(sql, Integer.class, documentId);
        return (count == null) ? 0 : count;
    }
}