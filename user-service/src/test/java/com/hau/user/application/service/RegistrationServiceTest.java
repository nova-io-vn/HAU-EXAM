package com.hau.user.application.service;

import com.hau.user.application.dto.RegistrationCommand;
import com.hau.user.application.port.out.ProcessedEventStore;
import com.hau.user.domain.repository.UserProfileRepository;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RegistrationServiceTest {
    @Test void duplicateEventIsSkippedIdempotently(){var repo=mock(UserProfileRepository.class);var inbox=mock(ProcessedEventStore.class);UUID eventId=UUID.randomUUID();when(inbox.exists(eventId)).thenReturn(true);var service=new RegistrationService(repo,inbox,Clock.systemUTC());var cmd=new RegistrationCommand(eventId,UUID.randomUUID(),UUID.randomUUID(),"GV1","User",LocalDate.of(1990,1,1),null,"u@hau.edu.vn",null,null,null);assertThat(service.createFromRegistration(cmd)).isFalse();verifyNoInteractions(repo);}
    @Test void createsPendingProfileAndRecordsEvent(){var repo=mock(UserProfileRepository.class);var inbox=mock(ProcessedEventStore.class);var service=new RegistrationService(repo,inbox,Clock.fixed(Instant.parse("2026-09-05T00:00:00Z"),ZoneOffset.UTC));var cmd=new RegistrationCommand(UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),"GV1","User",null,null,"u@hau.edu.vn",null,null,null);assertThat(service.createFromRegistration(cmd)).isTrue();verify(repo).save(argThat(u->u.getStatus()==com.hau.user.domain.model.UserStatus.PENDING_APPROVAL));verify(inbox).record(eq(cmd.eventId()),eq(RegistrationService.EVENT_TYPE),any());}
}
