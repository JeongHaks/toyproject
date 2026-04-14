package com.example.toyproject.rag.chunk.service;

import com.example.toyproject.rag.chunk.domain.Conversation;
import com.example.toyproject.rag.chunk.domain.Message;
import com.example.toyproject.rag.chunk.dto.AskRequest;
import com.example.toyproject.rag.chunk.dto.AskResponse;
import com.example.toyproject.rag.chunk.dto.RagAnswerResult;
import com.example.toyproject.rag.chunk.repo.ConversationRepository;
import com.example.toyproject.rag.chunk.repo.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional
public class ConversationAskService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final RagChunkTestService ragChunkTestService; // 기존 RAG answer 로직

    // 대화 요청 ID, 질문 내용
    public AskResponse ask(Long conversationId, AskRequest req) {

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("conversation not found"));

        Message userMsg = Message.user(conversationId, req.question());
        messageRepository.save(userMsg);

        /* 2. RAG 호출 */
        RagAnswerResult ragResult = ragChunkTestService.answer(
                req.question(),
                req.topK() != null ? req.topK() : 5
        );

        /* 3. ASSISTANT 메시지 저장 */
        Message assistantMsg = Message.assistant(
                conversationId,
                ragResult.answer(),
                ragResult.sourcesJson(),
                ragResult.distanceTop1()
        );
        messageRepository.save(assistantMsg);

        /* 4. conversation 갱신 */
        if ("새 채팅".equals(conversation.getTitle())) {
            conversation.setTitle(req.question());
        }
        conversationRepository.save(conversation);

        /* 5. UI 응답 */
        return new AskResponse(
                ragResult.answer(),
                ragResult.results()
        );
    }

    @Transactional
    public void deleteConversation(Long id) {
        if (!conversationRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "conversation not found");
        }

        // 1) FK 때문에 message 먼저 삭제
        messageRepository.deleteByConversationId(id);

        // 2) conversation 삭제
        conversationRepository.deleteById(id);
    }
}