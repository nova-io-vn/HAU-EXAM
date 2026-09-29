package com.notificationservice.presentation;

import com.notificationservice.application.port.out.EmailSender;
import com.notificationservice.domain.model.ContactStatus;
import com.notificationservice.infrastructure.persistence.entity.ContactRequestEntity;
import com.notificationservice.infrastructure.persistence.repository.JpaContactRequestRepository;
import com.notificationservice.presentation.controller.ContactController;
import com.notificationservice.presentation.request.ContactRequests;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ContactControllerTest {
    @Test
    void systemAdminReplyUsesPersistedContactRecipientAndPersistsOnlyAfterEmail() {
        var repository = mock(JpaContactRequestRepository.class);
        var email = mock(EmailSender.class);
        var contact = contact("persisted@example.test");
        when(repository.findById(contact.getId())).thenReturn(Optional.of(contact));
        when(repository.save(any(ContactRequestEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var controller = new ContactController(repository, email);

        var response = controller.reply(contact.getId(), new ContactRequests.Reply("Phản hồi đã xác nhận"));

        verify(email).send("persisted@example.test", "[HAU QM] Phản hồi yêu cầu hỗ trợ", "Phản hồi đã xác nhận");
        verify(email, times(1)).send(anyString(), anyString(), anyString());
        verify(repository).save(contact);
        assertThat(response.data().status()).isEqualTo(ContactStatus.REPLIED);
        assertThat(contact.getReplyMessage()).isEqualTo("Phản hồi đã xác nhận");
        assertThat(contact.getRepliedAt()).isNotNull();
    }

    private ContactRequestEntity contact(String email) {
        var contact = new ContactRequestEntity();
        contact.setId(UUID.randomUUID());
        contact.setName("Người gửi");
        contact.setEmail(email);
        contact.setSubject("Hỗ trợ");
        contact.setMessage("Nội dung");
        contact.setStatus(ContactStatus.NEW);
        contact.setCreatedAt(Instant.now());
        contact.setUpdatedAt(Instant.now());
        return contact;
    }
}
