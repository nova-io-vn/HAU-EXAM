package com.userservice.application.service;

import com.userservice.application.dto.ActorContext;
import com.userservice.application.port.out.UserEventPublisher;
import com.userservice.domain.exception.ForbiddenOperationException;
import com.userservice.domain.model.*;
import com.userservice.domain.repository.UserProfileRepository;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserAdministrationServiceTest {
    @Test void nonSystemAdminCannotApprove(){var repo=mock(UserProfileRepository.class);var publisher=mock(UserEventPublisher.class);var service=new UserAdministrationService(repo,publisher,Clock.systemUTC());var actor=new ActorContext(UUID.randomUUID(),Role.SUBJECT_ADMIN,"CNTT");assertThatThrownBy(()->service.approve(actor,UUID.randomUUID(),UUID.randomUUID())).isInstanceOf(ForbiddenOperationException.class);verifyNoInteractions(repo,publisher);}
    @Test void systemAdminApprovesAndPublishesEvent(){var repo=mock(UserProfileRepository.class);var publisher=mock(UserEventPublisher.class);Instant now=Instant.parse("2026-09-05T00:00:00Z");UUID id=UUID.randomUUID();var pending=UserProfile.pending(id,"GV1","User",null,null,"u@hau.edu.vn",null,null,"CNTT",now.minusSeconds(1));when(repo.findById(id)).thenReturn(Optional.of(pending));when(repo.save(any())).thenAnswer(i->i.getArgument(0));var service=new UserAdministrationService(repo,publisher,Clock.fixed(now,ZoneOffset.UTC));var result=service.approve(new ActorContext(UUID.randomUUID(),Role.SYSTEM_ADMIN,null),id,UUID.randomUUID());assertThat(result.getStatus()).isEqualTo(UserStatus.ACTIVE);verify(publisher).userApproved(eq(result),any());}

    @Test void systemAdminDeletesUserByAnonymizingAndPublishingStatus() {
        var repo = mock(UserProfileRepository.class);
        var publisher = mock(UserEventPublisher.class);
        Instant now = Instant.parse("2026-09-28T00:00:00Z");
        UUID actorId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID correlationId = UUID.randomUUID();
        var user = UserProfile.pending(userId, "GV10", "Nguyễn Văn A", null, "0901",
                "a@hau.edu.vn", "Hà Nội", "avatar", "CNTT", now.minusSeconds(10));
        when(repo.findById(userId)).thenReturn(Optional.of(user));
        when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var service = new UserAdministrationService(repo, publisher, Clock.fixed(now, ZoneOffset.UTC));
        var deleted = service.delete(new ActorContext(actorId, Role.SYSTEM_ADMIN, null), userId, correlationId);

        assertThat(deleted.getStatus()).isEqualTo(UserStatus.DELETED);
        assertThat(deleted.getFullName()).isEqualTo("Tài khoản đã xóa");
        assertThat(deleted.getPhone()).isNull();
        assertThat(deleted.getFacultyId()).isNull();
        assertThat(deleted.getEmail()).startsWith("deleted+").endsWith("@invalid.local");
        verify(publisher).statusChanged(deleted, correlationId);
    }

    @Test void cannotDeleteCurrentOrSystemAdminAccount() {
        var repo = mock(UserProfileRepository.class);
        var publisher = mock(UserEventPublisher.class);
        UUID actorId = UUID.randomUUID();
        var service = new UserAdministrationService(repo, publisher, Clock.systemUTC());
        var actor = new ActorContext(actorId, Role.SYSTEM_ADMIN, null);

        assertThatThrownBy(() -> service.delete(actor, actorId, UUID.randomUUID()))
                .isInstanceOf(ForbiddenOperationException.class);
        verifyNoInteractions(repo, publisher);

        UUID otherAdminId = UUID.randomUUID();
        var admin = UserProfile.bootstrapAdmin(otherAdminId, "ADMIN2", "Main Admin", "admin2@hau.edu.vn", null, Instant.now());
        when(repo.findById(otherAdminId)).thenReturn(Optional.of(admin));
        assertThatThrownBy(() -> service.delete(actor, otherAdminId, UUID.randomUUID()))
                .isInstanceOf(com.userservice.domain.exception.InvalidStatusTransitionException.class);
        verify(repo, never()).save(any());
        verifyNoInteractions(publisher);
    }
}
