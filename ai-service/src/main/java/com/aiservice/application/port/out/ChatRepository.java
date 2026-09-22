package com.aiservice.application.port.out;
import com.aiservice.domain.model.*;
import java.util.*;
public interface ChatRepository {
    ChatConversation save(ChatConversation conversation);
    Optional<ChatConversation> findConversation(UUID id);
    List<ChatConversation> findConversations(UUID userId);
    void deleteConversation(UUID id);
    ChatMessage saveMessage(ChatMessage message);
    List<ChatMessage> findMessages(UUID conversationId);
    void attachDocument(UUID conversationId, UUID documentId);
    void detachDocument(UUID conversationId, UUID documentId);
    List<UUID> findDocumentIds(UUID conversationId);
}
