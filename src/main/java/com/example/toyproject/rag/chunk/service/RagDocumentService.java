package com.example.toyproject.rag.chunk.service;

import com.example.toyproject.rag.chunk.ChunkSqlitter;
import com.example.toyproject.rag.chunk.embedding.OllamaChatClient;
import com.example.toyproject.rag.chunk.domain.Chunk;
import com.example.toyproject.rag.chunk.domain.Document;
import com.example.toyproject.rag.chunk.embedding.OllamaEmbeddingClient;
import com.example.toyproject.rag.chunk.repo.ChunkRepository;
import com.example.toyproject.rag.chunk.repo.DocumentRepository;
import com.example.toyproject.rag.chunk.util.PdfTextExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * RAG 문서 업로드/인덱싱 + 검색/답변 Service
 */
@Service
public class RagDocumentService {

    private static final Logger log = LoggerFactory.getLogger(RagDocumentService.class);

    private final DocumentRepository documentRepository;
    private final PdfTextExtractor pdfTextExtractor;
    private final ChunkRepository chunkRepository; // 업로드/저장용(JPA)
    private final OllamaEmbeddingClient ollamaEmbeddingClient;
    private final OllamaChatClient ollamaChatClient;
    private final JdbcTemplate jdbcTemplate;       // 벡터검색용(JdbcTemplate)

    // 생성자 기반 의존성 주입
    public RagDocumentService( DocumentRepository documentRepository,
                               PdfTextExtractor pdfTextExtractor,
                               ChunkRepository chunkRepository,
                               OllamaEmbeddingClient ollamaEmbeddingClient,
                               OllamaChatClient ollamaChatClient,
                               @Qualifier("ragJdbcTemplate") JdbcTemplate jdbcTemplate){
            this.documentRepository = documentRepository;
            this.pdfTextExtractor = pdfTextExtractor;
            this.chunkRepository = chunkRepository;
            this.ollamaEmbeddingClient = ollamaEmbeddingClient;
            this.ollamaChatClient = ollamaChatClient;
            this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * [문서 업로드 + 자동 인덱싱]
     */
    @Transactional("ragTransactionManager")
    public Long createDocument(MultipartFile file) {
        System.out.println("파일 업로드 서비스 2 : ");
        // 1) PDF 내용 → 텍스트 추출
        String extractedText;
        try {
            // 텍스트 추출 pdf 파일을 전달.
            extractedText = pdfTextExtractor.extract(file.getInputStream());
        } catch (Exception e) {
            throw new IllegalStateException("PDF 파일 처리 실패", e);
        }

        // 추출한 내용이 null 이거나, 비어져있거나
        if (extractedText == null || extractedText.isBlank()) {
            throw new IllegalStateException("PDF 텍스트 추출 결과가 비어 있습니다.");
        }

        // 2) 텍스트 추출 결과물을 documents DB 테이블에 저장
        Document document = new Document();
        document.setType("tech");                       // NOT NULL
        document.setTitle(file.getOriginalFilename());  // NOT NULL
        document.setContent(extractedText);            // NOT NULL
        document.setSourceType("manual");
        document.setVisibility("PRIVATE");
        document.setOwnerId(1L);

        // 텍스트 추출한 데이터 DB 테이블에 저장
        Document saved = documentRepository.save(document);

        // pdf 추출 후 documents 테이블에 내용 저장 후 저장행 id 가져오기
        Long documentId = saved.getId();

        // rag DB 연결해서 쿼리 전달.
        Integer existsInRag = jdbcTemplate.queryForObject(
                "select count(*) from documents where id = ?",
                Integer.class,
                documentId
        );

        System.out.println("### saved id=" + documentId + " / existsInRag=" + existsInRag);

        // 3) 텍스트 청킹(chunking) - pdf 파일 내용을 텍스트 추출 후 청크로 분할.
        List<String> chunks = ChunkSqlitter.splitToChunks(extractedText,1000,200);

        // 4) 청크 저장 + 임베딩 생성(문자->수치로) + embedding_768 업데이트
        int index = 0;
        for (String chunkText : chunks) {
            Chunk chunk = new Chunk();
            chunk.setDocumentId(documentId);
            chunk.setChunkIndex(index++);
            chunk.setContent(chunkText);

            // DB chunks 테이블에 문장 분할 내용 저장
            Chunk savedChunk = chunkRepository.save(chunk);

            // 문장을 수치화해서 만든 결과값 전달 받기
            float[] embedding = ollamaEmbeddingClient.embed768(savedChunk.getContent());
            savedChunk.setEmbedding768(embedding);

            // 수치값 전달 받은 후 저장하기.
            chunkRepository.save(savedChunk);
        }

        return saved.getId();
    }

    /**
     * [Top-K 검색] - JdbcTemplate로만 수행 (pgvector 연산은 Hibernate가 타입을 못 알아서 분리)
     */
    public List<Chunk> searchTopK(String question, int topK) {
        // 채팅 입력에 대한 유사근거 내용 찾기 서비스
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("question is blank");
        }

        float[] embedding = ollamaEmbeddingClient.embed768(question);
        String vec = toPgVectorLiteral(embedding);

        // JDBC에서 직접 캐스팅
        String sql = """
            SELECT id
                 , document_id
                 , chunk_index
                 , content
                 , embedding_768 <=> ?::vector AS distance
            FROM chunks
            WHERE 1=1
            AND document_id = ?
            ORDER BY embedding_768 <=> ?::vector
            LIMIT ?
        """;

        int k = normalizeTopK(topK);

        return jdbcTemplate.query(
                sql,
                ps -> {
                    ps.setString(1, vec);
                    ps.setString(2,vec);
                    ps.setInt(3, k);
                },
                (rs, rowNum) -> {
                    Chunk c = new Chunk();
                    c.setId(rs.getLong("id"));
                    c.setDocumentId(rs.getLong("document_id"));
                    c.setChunkIndex(rs.getInt("chunk_index"));
                    c.setContent(rs.getString("content"));
                    c.setDistance(rs.getDouble("distance"));
                    return c;
                }
        );

    }

    /**
     * [RAG 답변 생성]
     * - Top-K 검색 → context 구성 → LLM 답변 생성
     */
    public String answer(String question, int topK) {

        System.out.println("===== ANSWER METHOD START =====");

        if (question == null || question.trim().length() < 2) {
            return "질문을 조금 더 구체적으로 입력해주세요.";
        }


        // 반드시 JdbcTemplate 기반 searchTopK 사용
        List<Chunk> topChunks = searchTopK(question, topK);

        for (int i = 0; i < topChunks.size(); i++) {
            Chunk c = topChunks.get(i);
            System.out.println("[" + i + "] distance=" + c.getDistance());
            System.out.println(c.getContent());
            System.out.println("====================================");
        }

        if (topChunks == null || topChunks.isEmpty()) {
            return "문서에서 관련 내용을 찾을 수 없습니다.";
        }
//        if (topChunks.get(0).getDistance() > 0.8) {
//            return "문서에서 질문과 관련된 내용을 찾을 수 없습니다.";
//        }
        String system = """
        너는 RAG 기반 AI 어시스턴트다.
        
        답변 규칙:
        1. 반드시 제공된 CONTEXT에 포함된 정보만 사용해서 답변하라.
        2. CONTEXT에 없는 내용은 추측, 보완, 일반 지식으로 생성하지 마라.
        3. 질문의 답을 CONTEXT에서 찾을 수 없으면 반드시
           "제공된 문서에서 해당 내용을 찾을 수 없습니다."
           라고만 답하라.
        4. 답변은 CONTEXT의 표현을 최대한 그대로 활용하라.
        5. 문서에 없는 내용을 문서에 있는 것처럼 말하지 마라.
        
        출력 형식:
        - 한국어
        - 5줄 이내
        - 먼저 핵심 정의, 필요시 짧게 부연
        """;
        String context = buildContext(topChunks);
        System.out.println("---------------------------------------------------CONTEXT START --------");
        System.out.println(context);
        System.out.println("---------------------------------------------------CONTEXT STOP --------");
        String prompt = system + "\n\n" +
                "[CONTEXT START]\n" +
                context + "\n" +
                "[CONTEXT END]\n\n" +
                "[QUESTION]\n" +
                question + "\n\n" +
                "[INSTRUCTION]\n" +
                "반드시 CONTEXT만 근거로 답하라.\n" +
                "근거가 없으면 지정된 문장만 출력하라.\n";
        // llama3.1(8B) : 4~5GB MEM : 8GB 필요,, -> mistral(경량) -> gema:2b(초경량)
        String answer = ollamaChatClient.generate("gemma:2b", prompt);

        return answer;
    }

    /* -------------------------
       내부 유틸
       ------------------------- */
    // topK (유사 내용 몇 개 찾을 지)
    private int normalizeTopK(int topK) {
        int k = topK <= 0 ? 3 : topK;
        if (k > 10) k = 10;
        return k;
    }

    // 청크로 분할한 내용을 순번을 매기고, for문으로 옮긴다.
    private String buildContext(List<Chunk> chunks) {
        StringBuilder sb = new StringBuilder("[CONTEXT]\n");
        int i = 1;
        for (Chunk c : chunks) {
            String content = c.getContent().replace("\n:"," :").replace("\n"," ");
            sb.append("[")
              .append(i++)
              .append("]")
              .append(content)
              .append("\n\n");
        }
        return sb.toString();
    }

    // 문장 앞 뒤 공백 제거 / 길이가 너무 길면 문장 잘라준다.
    private String trimForPrompt(String text, int maxLen) {
        if (text == null) return "";
        String t = text.trim();
        if (t.length() <= maxLen) return t;
        return t.substring(0, maxLen) + "...";
    }

    /**
     * float[] -> pgvector 텍스트 리터럴 변환
     * 예) [0.12,-0.03,...]
     * Java [] 배열을 문자열 [] 형식으로 변환
     */
    private String toPgVectorLiteral(float[] v) {
        if (v == null || v.length == 0) {
            throw new IllegalArgumentException("embedding is empty");
        }
        StringBuilder sb = new StringBuilder(v.length * 8);
        sb.append('[');
        for (int i = 0; i < v.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(v[i]);
        }
        sb.append(']');
        return sb.toString();
    }
}
