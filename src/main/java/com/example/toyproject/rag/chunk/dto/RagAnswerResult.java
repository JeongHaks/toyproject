package com.example.toyproject.rag.chunk.dto;

public record RagAnswerResult(
        String answer,
        Object results,       // UI 근거 토글용 (Top-K 리스트 타입 그대로)
        String sourcesJson,   // message.sources_json 저장용
        Double distanceTop1   // message.distance_top1 저장용
) {}
