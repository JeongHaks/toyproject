package com.example.toyproject.rag.chunk.repo;

import com.example.toyproject.rag.chunk.domain.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // 특정 채팅방 메시지를 시간순으로 가져오기
    List<Message> findByConversationIdOrderByCreatedAtAsc(Long conversationId);

    @Modifying
    @Transactional
    @Query("delete from Message m where m.conversationId = :conversationId")
    int deleteByConversationId(Long conversationId);
}
