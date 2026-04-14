package com.example.toyproject.rag.chunk.repo;

import com.example.toyproject.rag.chunk.domain.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    List<Conversation> findTop50ByOrderByUpdatedAtDesc();

}
