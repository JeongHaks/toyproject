package com.example.toyproject.rag.chunk;
import com.example.toyproject.rag.chunk.domain.Conversation;
import com.example.toyproject.rag.chunk.domain.Message;
import com.example.toyproject.rag.chunk.dto.AskRequest;
import com.example.toyproject.rag.chunk.dto.AskResponse;
import com.example.toyproject.rag.chunk.dto.ConversationListItemDto;
import com.example.toyproject.rag.chunk.repo.ConversationRepository;
import com.example.toyproject.rag.chunk.repo.MessageRepository;
import com.example.toyproject.rag.chunk.service.ConversationAskService;
import com.example.toyproject.rag.chunk.service.RagChunkIngestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
public class ConversationApiController {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ConversationAskService conversationAskService;

    public ConversationApiController(ConversationRepository conversationRepository, MessageRepository messageRepository, ConversationAskService conversationAskService) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.conversationAskService = conversationAskService;
    }

    // 질문 내용 전달 받기.
    @PostMapping("/{id}/ask")
    public AskResponse ask(@PathVariable Long id, @RequestBody AskRequest req) {
        System.out.println("20260311  (1) ");
        return conversationAskService.ask(id, req);
    }

    // 대화창 목록
    @GetMapping
    public List<ConversationListItemDto> list() {
        return conversationRepository.findTop50ByOrderByUpdatedAtDesc().stream()
                .map(c -> new ConversationListItemDto(
                        c.getId(),
                        c.getTitle(),
                        c.getUpdatedAt()
                ))
                .toList();
    }

    @PostMapping
    public CreateConversationResponse create() {
        Conversation c = new Conversation(); // title 기본값 "새 채팅"
        Conversation saved = conversationRepository.save(c);
        return new CreateConversationResponse(saved.getId(), saved.getTitle());
    }

    public record CreateConversationResponse(Long id, String title) {}


    //  채팅방 1개 + 메시지 목록 조회 (이번 단계)
    @GetMapping("/{id}")
    public ConversationDetailResponse getOne(@PathVariable Long id) {
        Conversation convo = conversationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "conversation not found: " + id));

        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(id);

        // Message 엔티티를 그대로 내려도 되지만, 나중 확장 대비 DTO로 내려줌
        List<MessageDto> messageDtos = messages.stream()
                .map(m -> new MessageDto(
                        m.getId(),
                        m.getRole(),
                        m.getContent(),
                        m.getSourcesJson(),
                        m.getDistanceTop1(),
                        m.getCreatedAt()
                ))
                .toList();

        return new ConversationDetailResponse(
                new ConversationDto(convo.getId(), convo.getTitle(), convo.getCreatedAt(), convo.getUpdatedAt()),
                messageDtos
        );
    }

    public record ConversationDetailResponse(ConversationDto conversation, List<MessageDto> messages) {}

    public record ConversationDto(Long id, String title,
                                  java.time.OffsetDateTime createdAt,
                                  java.time.OffsetDateTime updatedAt) {}

    public record MessageDto(Long id, String role, String content,
                             String sourcesJson, Double distanceTop1,
                             java.time.OffsetDateTime createdAt) {}

    // 채팅방 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConversation(@PathVariable Long id) {
        conversationAskService.deleteConversation(id);
        return ResponseEntity.noContent().build();
    }
}
