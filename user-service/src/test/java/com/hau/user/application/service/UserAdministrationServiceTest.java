package com.hau.user.application.service;

import com.hau.user.application.dto.ActorContext;
import com.hau.user.application.port.out.UserEventPublisher;
import com.hau.user.domain.exception.ForbiddenOperationException;
import com.hau.user.domain.model.*;
import com.hau.user.domain.repository.UserProfileRepository;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserAdministrationServiceTest {
    @Test void nonSystemAdminCannotApprove(){var repo=mock(UserProfileRepository.class);var publisher=mock(UserEventPublisher.class);var service=new UserAdministrationService(repo,publisher,Clock.systemUTC());var actor=new ActorContext(UUID.randomUUID(),Role.SUBJECT_ADMIN,"CNTT");assertThatThrownBy(()->service.approve(actor,UUID.randomUUID(),UUID.randomUUID())).isInstanceOf(ForbiddenOperationException.class);verifyNoInteractions(repo,publisher);}
    @Test void systemAdminApprovesAndPublishesEvent(){var repo=mock(UserProfileRepository.class);var publisher=mock(UserEventPublisher.class);Instant now=Instant.parse("2026-09-05T00:00:00Z");UUID id=UUID.randomUUID();var pending=UserProfile.pending(id,"GV1","User",null,null,"u@hau.edu.vn",null,null,null,now.minusSeconds(1));when(repo.findById(id)).thenReturn(Optional.of(pending));when(repo.save(any())).thenAnswer(i->i.getArgument(0));var service=new UserAdministrationService(repo,publisher,Clock.fixed(now,ZoneOffset.UTC));var result=service.approve(new ActorContext(UUID.randomUUID(),Role.SYSTEM_ADMIN,null),id,UUID.randomUUID());assertThat(result.getStatus()).isEqualTo(UserStatus.ACTIVE);verify(publisher).userApproved(eq(result),any());}
}
