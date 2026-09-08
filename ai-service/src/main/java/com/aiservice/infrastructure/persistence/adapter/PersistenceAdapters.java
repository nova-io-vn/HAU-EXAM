package com.aiservice.infrastructure.persistence.adapter;

import com.aiservice.application.port.out.*;
import com.aiservice.domain.model.*;
import com.aiservice.infrastructure.persistence.entity.*;
import com.aiservice.infrastructure.persistence.mapper.*;
import com.aiservice.infrastructure.persistence.repository.*;

import java.time.Instant;
import java.util.*;

import org.springframework.stereotype.Component;

public final class PersistenceAdapters {
    private PersistenceAdapters() {
    }

    @Component
    public static class Documents implements DocumentRepository {
        private final DocumentJpaRepository r;
        private final DocumentMapper m;

        public Documents(DocumentJpaRepository r, DocumentMapper m) {
            this.r = r;
            this.m = m;
        }

        public com.aiservice.application.model.WorkspacePage<DocumentMetadata> findByOwner(UUID owner, int page, int size) {
            var p = r.findByOwnerId(owner, org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by("createdAt").descending().and(org.springframework.data.domain.Sort.by("id"))));
            return new com.aiservice.application.model.WorkspacePage<>(p.getContent().stream().map(m::toDomain).toList(), page, size, p.getTotalElements(), p.getTotalPages());
        }

        public DocumentMetadata save(DocumentMetadata d) {
            return m.toDomain(r.save(m.toEntity(d)));
        }

        public Optional<DocumentMetadata> findById(UUID id) {
            return r.findById(id).map(m::toDomain);
        }
    }

    @Component
    public static class Jobs implements AiJobRepository {
        private final AiJobJpaRepository r;
        private final AiJobMapper m;

        public Jobs(AiJobJpaRepository r, AiJobMapper m) {
            this.r = r;
            this.m = m;
        }

        public com.aiservice.application.model.WorkspacePage<AiJob> findByOwner(UUID owner, int page, int size) {
            var p = r.findByRequestedBy(owner, org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by("createdAt").descending().and(org.springframework.data.domain.Sort.by("id"))));
            return new com.aiservice.application.model.WorkspacePage<>(p.getContent().stream().map(m::toDomain).toList(), page, size, p.getTotalElements(), p.getTotalPages());
        }

        public AiJob save(AiJob j) {
            var e = m.toEntity(j);
            r.findById(j.id()).ifPresent(old -> e.version = old.version);
            return m.toDomain(r.save(e));
        }

        public Optional<AiJob> findById(UUID id) {
            return r.findById(id).map(m::toDomain);
        }
    }

    @Component
    public static class Results implements AiResultRepository {
        private final AiResultJpaRepository r;

        public Results(AiResultJpaRepository r) {
            this.r = r;
        }

        public String save(UUID job, String json) {
            var e = new AiResultEntity();
            e.id = UUID.randomUUID();
            e.jobId = job;
            e.resultJson = json;
            e.createdAt = Instant.now();
            r.save(e);
            return "db:ai-results:" + job;
        }

        public Optional<String> findByJobId(UUID id) {
            return r.findByJobId(id).map(e -> e.resultJson);
        }
    }

    @Component
    public static class Inbox implements ProcessedEventRepository {
        private final ProcessedEventJpaRepository r;

        public Inbox(ProcessedEventJpaRepository r) {
            this.r = r;
        }

        public boolean exists(UUID id) {
            return r.existsById(id);
        }

        public void record(UUID id, String type) {
            var e = new ProcessedEventEntity();
            e.eventId = id;
            e.eventType = type;
            e.processedAt = Instant.now();
            r.save(e);
        }
    }

    @Component
    public static class Chats implements ChatRepository {
        private final ChatConversationJpaRepository conversations;
        private final ChatMessageJpaRepository messages;
        private final ChatAttachmentJpaRepository attachments;
        public Chats(ChatConversationJpaRepository c, ChatMessageJpaRepository m, ChatAttachmentJpaRepository a) { conversations=c; messages=m; attachments=a; }
        public ChatConversation save(ChatConversation d) { var e=new ChatConversationEntity(); e.id=d.id(); e.userId=d.userId(); e.title=d.title(); e.createdAt=d.createdAt(); e.updatedAt=d.updatedAt(); conversations.save(e); return d; }
        public Optional<ChatConversation> findConversation(UUID id) { return conversations.findById(id).map(e->new ChatConversation(e.id,e.userId,e.title,e.createdAt,e.updatedAt)); }
        public List<ChatConversation> findConversations(UUID u) { return conversations.findByUserIdOrderByUpdatedAtDesc(u).stream().map(e->new ChatConversation(e.id,e.userId,e.title,e.createdAt,e.updatedAt)).toList(); }
        public void deleteConversation(UUID id) { conversations.deleteById(id); }
        public ChatMessage saveMessage(ChatMessage d) { var e=new ChatMessageEntity(); e.id=d.id(); e.conversationId=d.conversationId(); e.role=d.role(); e.content=d.content(); e.status=d.status(); e.createdAt=d.createdAt(); messages.save(e); return d; }
        public List<ChatMessage> findMessages(UUID id) { return messages.findByConversationIdOrderByCreatedAtAsc(id).stream().map(e->new ChatMessage(e.id,e.conversationId,e.role,e.content,e.status,e.createdAt)).toList(); }
        public void attachDocument(UUID c, UUID d) { var e=new ChatAttachmentEntity(); e.id=UUID.randomUUID(); e.conversationId=c; e.documentId=d; e.attachedAt=Instant.now(); attachments.save(e); }
        public void detachDocument(UUID c, UUID d) { attachments.deleteByConversationIdAndDocumentId(c,d); }
        public List<UUID> findDocumentIds(UUID c) { return attachments.findByConversationId(c).stream().map(e->e.documentId).toList(); }
    }
}
