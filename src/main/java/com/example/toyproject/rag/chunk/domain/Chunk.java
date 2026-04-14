package com.example.toyproject.rag.chunk.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * chunks 테이블 매핑 엔티티
 *
 * 역할:
 * - 문서(document) 하나를 여러 청크로 나눠 저장
 * - 지금 단계에서는 embedding은 다루지 않는다
 */
@Getter
@Setter
@Entity
@Table(name = "chunks",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"document_id", "chunk_index"}
        ))
public class Chunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 어떤 문서의 청크인지 */
    @Column(name = "document_id", nullable = false)
    private Long documentId;

    /** 문서 내 청크 순서 (0부터 시작) */
    @Column(name = "chunk_index", nullable = false)
    private int chunkIndex;

    @Column(name = "embedding_768")
    private float[] embedding768;

    /** 청크 본문 */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    private Double distance;

    /* getter / setter는 IDE 자동 생성 */
}