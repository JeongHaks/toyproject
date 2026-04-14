package com.example.toyproject.rag.chunk.dto;

public record OllamaGenerateRequest(String model, String prompt, boolean stream) {
}
