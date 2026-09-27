package com.notificationservice.application.service;

import com.notificationservice.infrastructure.persistence.entity.SupportConversationEntity;
import com.notificationservice.infrastructure.persistence.repository.SupportConversationRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
class DirectConversationWriter {
    private final SupportConversationRepository conversations;

    DirectConversationWriter(SupportConversationRepository conversations) {
        this.conversations = conversations;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    SupportConversationEntity insert(SupportConversationEntity conversation) {
        return conversations.saveAndFlush(conversation);
    }
}
