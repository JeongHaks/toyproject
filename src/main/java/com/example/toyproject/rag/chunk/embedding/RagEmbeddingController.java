package com.example.toyproject.rag.chunk.embedding;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rag")
public class RagEmbeddingController {

    private final ChunkEmbeddingService service;

    public RagEmbeddingController(ChunkEmbeddingService service) {
        this.service = service;
    }

    @GetMapping("/embed-one")
    public ChunkEmbeddingService.EmbedOneResult embedOne(@RequestParam long documentId) {
        return service.embedOneChunk(documentId);
    }

    @GetMapping("/embed-all")
    public ChunkEmbeddingService.EmbedAllResult embedAll(@RequestParam long documentId) {
        return service.embedAllChunks(documentId);
    }
}
