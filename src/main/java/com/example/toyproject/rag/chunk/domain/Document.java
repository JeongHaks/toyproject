package com.example.toyproject.rag.chunk.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 문서 유형
     * - RAG에서는 'tech'로 고정 사용
     * - (기존 도메인과 충돌 방지)
     */
    @Column(nullable = false)
    private String type;

    /**
     * 문서 제목 (PDF 파일명)
     */
    @Column(nullable = false)
    private String title;

    /**
     * 문서 원문
     * - 지금 단계: "PDF 업로드됨" 같은 임시 문자열
     * - 다음 단계에서 PDF 텍스트 전체로 대체
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 출처 구분
     * - manual / post / comment
     * - RAG 업로드는 'manual'로 고정
     */
    @Column(name = "source_type")
    private String sourceType;

    /**
     * 공개 여부 (RAG 데모는 PRIVATE 고정)
     */
    private String visibility = "PRIVATE";

    /**
     * 문서 소유자
     * - 지금 단계에서는 로그인 연동 안 하므로
     * 임시로 1L 사용
     */
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}