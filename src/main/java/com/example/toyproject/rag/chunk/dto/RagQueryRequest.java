package com.example.toyproject.rag.chunk.dto;


import lombok.Getter;
import lombok.Setter;

/**
 * /rag/query 요청 DTO
 * - question: 사용자가 입력한 질문
 * - topK: 몇 개의 청크를 가져올지 (예: 3~5)
 */
@Getter
@Setter
public class RagQueryRequest {
    private String question;
    private Integer topK;
}