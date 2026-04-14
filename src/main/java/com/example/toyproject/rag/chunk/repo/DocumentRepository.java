package com.example.toyproject.rag.chunk.repo;

import com.example.toyproject.rag.chunk.domain.Document;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * documents 테이블 접근 Repository
 *
 * 현재 역할:
 * - Document 엔티티를 DB에 저장/조회하기 위한 기본 CRUD 제공
 * - 아직 커스텀 쿼리는 없음
 */
public interface DocumentRepository extends JpaRepository<Document, Long> {
}
