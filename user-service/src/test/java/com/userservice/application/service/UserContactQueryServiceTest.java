package com.userservice.application.service;

import com.userservice.domain.model.Role;
import com.userservice.domain.model.UserProfile;
import com.userservice.domain.repository.UserProfileRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserContactQueryServiceTest {
    private final UserProfileRepository users = mock(UserProfileRepository.class);
    private final UserContactQueryService service = new UserContactQueryService(users);

    @Test
    void systemAdminReceivesUserNamesForSupportConversations() {
        UUID adminId = UUID.randomUUID();
        UserProfile user = profile(UUID.randomUUID(), "Nguyễn Văn An", Role.USER);
        UserProfile subjectAdmin = profile(UUID.randomUUID(), "Trần Thu Hà", Role.SUBJECT_ADMIN);
        when(users.findActiveAudience(Role.USER, null)).thenReturn(List.of(user));
        when(users.findActiveAudience(Role.SUBJECT_ADMIN, null)).thenReturn(List.of(subjectAdmin));

        var contacts = service.contacts(adminId, "SYSTEM_ADMIN", null);

        assertEquals(List.of("Nguyễn Văn An", "Trần Thu Hà"), contacts.stream().map(UserContactQueryService.ChatContact::displayName).toList());
        verify(users).findActiveAudience(Role.USER, null);
        verify(users).findActiveAudience(Role.SUBJECT_ADMIN, null);
    }

    @Test
    void regularUserOnlyReceivesSystemAdministrators() {
        UUID userId = UUID.randomUUID();
        UserProfile admin = profile(UUID.randomUUID(), "Quản trị HAU", Role.SYSTEM_ADMIN);
        when(users.findActiveAudience(Role.SYSTEM_ADMIN, null)).thenReturn(List.of(admin));

        var contacts = service.contacts(userId, "USER", "CNTT");

        assertEquals(1, contacts.size());
        assertEquals("Quản trị HAU", contacts.getFirst().displayName());
        verify(users).findActiveAudience(Role.SYSTEM_ADMIN, null);
    }

    private UserProfile profile(UUID id, String name, Role role) {
        UserProfile profile = mock(UserProfile.class);
        when(profile.getId()).thenReturn(id);
        when(profile.getFullName()).thenReturn(name);
        when(profile.getRole()).thenReturn(role);
        when(profile.getFacultyId()).thenReturn("CNTT");
        return profile;
    }
}
