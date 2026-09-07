package com.notificationservice.presentation.response;
import com.notificationservice.domain.model.ContactStatus;
import com.notificationservice.infrastructure.persistence.entity.ContactRequestEntity;
import java.time.Instant;import java.util.UUID;
public record ContactResponse(UUID id,String name,String email,String subject,String message,String facultyId,ContactStatus status,String adminNote,String replyMessage,Instant createdAt,Instant updatedAt,Instant repliedAt){public static ContactResponse from(ContactRequestEntity c){return new ContactResponse(c.getId(),c.getName(),c.getEmail(),c.getSubject(),c.getMessage(),c.getFacultyId(),c.getStatus(),c.getAdminNote(),c.getReplyMessage(),c.getCreatedAt(),c.getUpdatedAt(),c.getRepliedAt());}}
