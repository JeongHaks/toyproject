package com.example.toyproject.rag.chunk.dto;


import java.time.OffsetDateTime;

public record ConversationListItemDto(Long id, String title, OffsetDateTime updatedAt) {

}
