package com.userservice.application.service;

import com.userservice.domain.model.Role;
import com.userservice.domain.repository.UserProfileRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class AudienceQueryServiceTest {
    @Test
    void supportsRoleFacultyAndCombinedAudienceQueries() {
        UserProfileRepository users = mock(UserProfileRepository.class);
        when(users.findActiveAudience(any(), any())).thenReturn(List.of());
        var service = new AudienceQueryService(users);

        service.resolve("USER", null);
        service.resolve(null, "CNTT");
        service.resolve("SUBJECT_ADMIN", "CNTT");

        verify(users).findActiveAudience(Role.USER, null);
        verify(users).findActiveAudience(null, "CNTT");
        verify(users).findActiveAudience(Role.SUBJECT_ADMIN, "CNTT");
    }

    @Test
    void rejectsAnUnboundedAudience() {
        var service = new AudienceQueryService(mock(UserProfileRepository.class));
        assertThatThrownBy(() -> service.resolve(null, null)).isInstanceOf(IllegalArgumentException.class);
    }
}
