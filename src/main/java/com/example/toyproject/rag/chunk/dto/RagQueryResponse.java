package com.example.toyproject.rag.chunk.dto;

import java.util.List;

public class RagQueryResponse {

    private final String answer;
    private final List<ChunkResult> results;

    public RagQueryResponse(String answer, List<ChunkResult> results) {
        this.answer = answer;
        this.results = results;
    }

    public String getAnswer() {
        return answer;
    }

    public List<ChunkResult> getResults() {
        return results;
    }

    public static class ChunkResult {
        private final Long chunkId;
        private final Long documentId;
        private final Integer chunkIndex;
        private final String contentPreview;

        public ChunkResult(Long chunkId, Long documentId, Integer chunkIndex, String contentPreview) {
            this.chunkId = chunkId;
            this.documentId = documentId;
            this.chunkIndex = chunkIndex;
            this.contentPreview = contentPreview;
        }

        public Long getChunkId() { return chunkId; }
        public Long getDocumentId() { return documentId; }
        public Integer getChunkIndex() { return chunkIndex; }
        public String getContentPreview() { return contentPreview; }
    }
}
