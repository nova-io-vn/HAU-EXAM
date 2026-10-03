package com.notificationservice.application.service;

import com.notificationservice.application.dto.UserContact;
import com.notificationservice.application.port.out.SupportImageStorage;
import com.notificationservice.application.port.out.UserContactResolver;
import com.notificationservice.application.port.out.AudienceResolver;
import com.notificationservice.application.dto.Recipient;
import com.notificationservice.domain.model.BroadcastGroup;
import com.notificationservice.domain.model.SupportStatus;
import com.notificationservice.infrastructure.persistence.entity.SupportConversationEntity;
import com.notificationservice.infrastructure.persistence.entity.SupportMessageEntity;
import com.notificationservice.infrastructure.persistence.repository.SupportAttachmentRepository;
import com.notificationservice.infrastructure.persistence.repository.SupportConversationRepository;
import com.notificationservice.infrastructure.persistence.repository.SupportMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.notificationservice.domain.exception.ForbiddenNotificationAccessException;

@ExtendWith(MockitoExtension.class)
class SupportChatServiceTest {
    @Mock SupportConversationRepository conversations;
    @Mock SupportMessageRepository messages;
    @Mock SupportAttachmentRepository attachments;
    @Mock SupportImageStorage storage;
    @Mock SimpMessagingTemplate realtime;
    @Mock UserContactResolver contacts;
    @Mock DirectConversationWriter writer;
    @Mock AudienceResolver audience;

    private SupportChatService service;

    @BeforeEach
    void setUp() {
        service = new SupportChatService(conversations, messages, attachments, storage, realtime, contacts, writer, audience);
    }

    @Test
    void userCanCreateDirectConversationWithSubjectAdminInSameFaculty() {
        UUID user = UUID.randomUUID();
        UUID subjectAdmin = UUID.randomUUID();
        when(contacts.resolve(subjectAdmin)).thenReturn(contact(subjectAdmin, "SUBJECT_ADMIN", "CNTT"));
        when(conversations.findByDirectParticipantKey(directKey(user, subjectAdmin))).thenReturn(Optional.empty());
        when(writer.insert(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var created = service.create(user, "USER", "CNTT", "Trao đổi", "", subjectAdmin);

        assertThat(created.createdByUserId()).isEqualTo(user);
        assertThat(created.assignedAdminId()).isEqualTo(subjectAdmin);
    }

    @Test
    void subjectAdminCanReuseConversationCreatedByUser() {
        UUID user = UUID.randomUUID();
        UUID subjectAdmin = UUID.randomUUID();
        var existing = conversation(user, subjectAdmin);
        existing.setDeletedAtAdmin(Instant.now());
        when(contacts.resolve(user)).thenReturn(contact(user, "USER", "CNTT"));
        when(conversations.findByDirectParticipantKey(directKey(subjectAdmin, user))).thenReturn(Optional.of(existing));
        when(conversations.save(existing)).thenReturn(existing);
        when(messages.findFirstByConversationIdOrderByCreatedAtDesc(existing.getId())).thenReturn(Optional.empty());
        when(messages.countByConversationIdAndSenderIdNotAndReadAtIsNull(existing.getId(), subjectAdmin)).thenReturn(0L);

        var resolved = service.create(subjectAdmin, "SUBJECT_ADMIN", "CNTT", "Trao đổi", "", user);

        assertThat(resolved.id()).isEqualTo(existing.getId());
        assertThat(existing.getDeletedAtAdmin()).isNull();
        verify(writer, never()).insert(any());
    }

    @Test
    void subjectAdminCanCreateConversationWithSystemAdmin() {
        UUID subjectAdmin = UUID.randomUUID();
        UUID systemAdmin = UUID.randomUUID();
        when(contacts.resolve(systemAdmin)).thenReturn(contact(systemAdmin, "SYSTEM_ADMIN", null));
        when(conversations.findByDirectParticipantKey(directKey(subjectAdmin, systemAdmin))).thenReturn(Optional.empty());
        when(writer.insert(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var created = service.create(subjectAdmin, "SUBJECT_ADMIN", "CNTT", "Trao đổi", "", systemAdmin);

        assertThat(created.createdByUserId()).isEqualTo(subjectAdmin);
        assertThat(created.assignedAdminId()).isEqualTo(systemAdmin);
    }

    @Test
    void uniqueKeyRaceResolvesCanonicalConversation() {
        UUID user = UUID.randomUUID();
        UUID subjectAdmin = UUID.randomUUID();
        var canonical = conversation(subjectAdmin, user);
        String key = directKey(user, subjectAdmin);
        canonical.setDirectParticipantKey(key);
        when(contacts.resolve(subjectAdmin)).thenReturn(contact(subjectAdmin, "SUBJECT_ADMIN", "CNTT"));
        when(conversations.findByDirectParticipantKey(key)).thenReturn(Optional.empty(), Optional.of(canonical));
        when(writer.insert(any())).thenThrow(new DataIntegrityViolationException("duplicate participant key"));
        when(conversations.save(canonical)).thenReturn(canonical);
        when(messages.findFirstByConversationIdOrderByCreatedAtDesc(canonical.getId())).thenReturn(Optional.empty());
        when(messages.countByConversationIdAndSenderIdNotAndReadAtIsNull(canonical.getId(), user)).thenReturn(0L);

        var resolved = service.create(user, "USER", "CNTT", "Trao đổi", "", subjectAdmin);

        assertThat(resolved.id()).isEqualTo(canonical.getId());
    }

    @Test
    void participantCanSendAndWebsocketNotifiesBothStableUserIds() {
        UUID user = UUID.randomUUID();
        UUID subjectAdmin = UUID.randomUUID();
        var conversation = conversation(user, subjectAdmin);
        when(conversations.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(messages.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(conversations.save(conversation)).thenReturn(conversation);
        when(attachments.findByMessageId(any())).thenReturn(java.util.List.of());

        var sent = service.send(conversation.getId(), user, "USER", "CNTT", "Xin chào", null);

        assertThat(sent.content()).isEqualTo("Xin chào");
        verify(realtime).convertAndSendToUser(org.mockito.ArgumentMatchers.eq(user.toString()), org.mockito.ArgumentMatchers.eq("/queue/support"), any());
        verify(realtime).convertAndSendToUser(org.mockito.ArgumentMatchers.eq(subjectAdmin.toString()), org.mockito.ArgumentMatchers.eq("/queue/support"), any());
    }

    @Test
    void senderCanRevokeOwnMessageAndRecipientIsNotified() {
        UUID user=UUID.randomUUID(),recipient=UUID.randomUUID();var conversation=conversation(user,recipient);var message=new SupportMessageEntity();message.setId(UUID.randomUUID());message.setConversationId(conversation.getId());message.setSenderId(user);message.setSenderRole("USER");message.setContent("Nội dung");message.setCreatedAt(Instant.now());
        when(messages.findById(message.getId())).thenReturn(Optional.of(message));when(conversations.findById(conversation.getId())).thenReturn(Optional.of(conversation));when(messages.save(message)).thenReturn(message);
        var result=service.revokeMessage(message.getId(),user,"USER","CNTT");
        assertThat(result.deleted()).isTrue();assertThat(result.content()).isNull();verify(realtime).convertAndSendToUser(org.mockito.ArgumentMatchers.eq(recipient.toString()),org.mockito.ArgumentMatchers.eq("/queue/support"),any());
    }

    @Test
    void anotherParticipantCannotRevokeSendersMessage() {
        UUID user=UUID.randomUUID(),recipient=UUID.randomUUID();var conversation=conversation(user,recipient);var message=new SupportMessageEntity();message.setId(UUID.randomUUID());message.setConversationId(conversation.getId());message.setSenderId(user);message.setCreatedAt(Instant.now());
        when(messages.findById(message.getId())).thenReturn(Optional.of(message));when(conversations.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        assertThrows(ForbiddenNotificationAccessException.class,()->service.revokeMessage(message.getId(),recipient,"SUBJECT_ADMIN","CNTT"));
    }

    @Test
    void typingIsTransientAndTargetsOnlyTheOtherParticipant() {
        UUID user=UUID.randomUUID(),recipient=UUID.randomUUID();var conversation=conversation(user,recipient);
        when(conversations.findById(conversation.getId())).thenReturn(Optional.of(conversation));

        service.typing(conversation.getId(),user,"USER","CNTT",true);

        verify(realtime).convertAndSendToUser(org.mockito.ArgumentMatchers.eq(recipient.toString()),org.mockito.ArgumentMatchers.eq("/queue/support"),any());
        verify(messages,never()).save(any());
    }

    @Test
    void nonParticipantCannotSpoofTypingEvenWithSystemAdminRole() {
        UUID user=UUID.randomUUID(),recipient=UUID.randomUUID(),outsider=UUID.randomUUID();var conversation=conversation(user,recipient);
        when(conversations.findById(conversation.getId())).thenReturn(Optional.of(conversation));

        assertThrows(ForbiddenNotificationAccessException.class,()->service.typing(conversation.getId(),outsider,"SYSTEM_ADMIN",null,true));
        verify(realtime,never()).convertAndSendToUser(any(),any(),any());
    }

    @Test
    void subjectAdminBroadcastCountIsResolvedFromOwnFacultyOnly() {
        UUID admin=UUID.randomUUID(),ownUser=UUID.randomUUID();
        when(audience.resolve("USER","CNTT")).thenReturn(java.util.List.of(new Recipient(ownUser,"u@example.test")));

        assertThat(service.broadcastCount(admin,"SUBJECT_ADMIN","CNTT",BroadcastGroup.FACULTY_USERS)).isEqualTo(1);
        verify(audience).resolve("USER","CNTT");
        assertThrows(ForbiddenNotificationAccessException.class,()->service.broadcastCount(admin,"SUBJECT_ADMIN","CNTT",BroadcastGroup.ALL));
    }

    @Test
    void userCannotBroadcastAndSystemAdminGroupsAreServerResolved() {
        UUID admin=UUID.randomUUID(),user=UUID.randomUUID(),subjectAdmin=UUID.randomUUID();
        when(audience.resolve("USER",null)).thenReturn(java.util.List.of(new Recipient(user,"u@example.test")));
        when(audience.resolve("SUBJECT_ADMIN",null)).thenReturn(java.util.List.of(new Recipient(subjectAdmin,"a@example.test")));
        when(audience.resolve("SYSTEM_ADMIN",null)).thenReturn(java.util.List.of(new Recipient(admin,"admin@example.test")));

        assertThat(service.broadcastCount(admin,"SYSTEM_ADMIN",null,BroadcastGroup.ALL)).isEqualTo(2);
        assertThrows(ForbiddenNotificationAccessException.class,()->service.broadcastCount(user,"USER","CNTT",BroadcastGroup.USER));
    }

    @Test
    void facultyBroadcastPersistsExactlyOneMessageForEachServerResolvedRecipient() {
        UUID admin=UUID.randomUUID(),first=UUID.randomUUID(),second=UUID.randomUUID();
        when(audience.resolve("USER","CNTT")).thenReturn(java.util.List.of(new Recipient(first,"1@example.test"),new Recipient(second,"2@example.test")));
        when(contacts.resolve(any())).thenAnswer(invocation->contact(invocation.getArgument(0),"USER","CNTT"));
        when(conversations.findByDirectParticipantKey(any())).thenReturn(Optional.empty());
        var inserted=new java.util.HashMap<UUID,SupportConversationEntity>();
        when(writer.insert(any())).thenAnswer(invocation->{SupportConversationEntity value=invocation.getArgument(0);inserted.put(value.getId(),value);return value;});
        when(conversations.findById(any())).thenAnswer(invocation->Optional.ofNullable(inserted.get(invocation.getArgument(0))));
        when(messages.save(any())).thenAnswer(invocation->invocation.getArgument(0));
        when(conversations.save(any())).thenAnswer(invocation->invocation.getArgument(0));
        when(attachments.findByMessageId(any())).thenReturn(java.util.List.of());

        assertThat(service.broadcast(admin,"SUBJECT_ADMIN","CNTT",BroadcastGroup.FACULTY_USERS,"Thông báo",null)).isEqualTo(2);
        verify(messages,times(2)).save(any());
    }

    private static UserContact contact(UUID id, String role, String faculty) {
        return new UserContact(id, "GV001", "Người dùng", "user@example.test", faculty, role, null);
    }

    private static SupportConversationEntity conversation(UUID creator, UUID recipient) {
        var value = new SupportConversationEntity();
        value.setId(UUID.randomUUID());
        value.setCreatedByUserId(creator);
        value.setCreatedByRole("USER");
        value.setAssignedAdminId(recipient);
        value.setDirectParticipantKey(directKey(creator, recipient));
        value.setFacultyId("CNTT");
        value.setSubject("Trao đổi");
        value.setStatus(SupportStatus.OPEN);
        value.setCreatedAt(Instant.now());
        value.setUpdatedAt(Instant.now());
        value.setLastMessageAt(Instant.now());
        return value;
    }

    private static String directKey(UUID first, UUID second) {
        return first.toString().compareTo(second.toString()) < 0 ? first + ":" + second : second + ":" + first;
    }
}
