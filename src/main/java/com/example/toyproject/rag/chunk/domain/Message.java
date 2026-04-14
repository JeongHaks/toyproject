package com.example.toyproject.rag.chunk.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "message")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "conversation_id", nullable = false)
    private Long conversationId;

    @Column(nullable = false, length = 20)
    private String role; // USER / ASSISTANT / SYSTEM

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "sources_json", columnDefinition = "text")
    private String sourcesJson; // 일단 String으로 저장(나중에 JSON 매핑 가능)

    @Column(name = "distance_top1")
    private Double distanceTop1;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }
    public static Message user(Long conversationId, String content) {
        Message m = new Message();
        m.setConversationId(conversationId);
        m.setRole("USER");
        m.setContent(content);
        return m;
    }

    public static Message assistant(Long conversationId, String content, String sourcesJson, Double distanceTop1) {
        Message m = new Message();
        m.setConversationId(conversationId);
        m.setRole("ASSISTANT");
        m.setContent(content);
        m.setSourcesJson(sourcesJson);
        m.setDistanceTop1(distanceTop1);
        return m;
    }
}
