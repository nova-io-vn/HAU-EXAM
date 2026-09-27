package com.userservice.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.userservice.domain.model.Role;
import com.userservice.domain.model.UserProfile;
import com.userservice.domain.model.UserStatus;
import com.userservice.domain.repository.UserProfileRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserContactQueryServiceTest {
    private final UserProfileRepository users = mock(UserProfileRepository.class);
    private final UserContactQueryService service = new UserContactQueryService(users);

    @Test
    void systemAdminReceivesUserNamesForSupportConversations() {
        when(users.findActiveAudience(Role.USER, null)).thenReturn(List.of(profile("Nguyễn Văn An", Role.USER)));
        when(users.findActiveAudience(Role.SUBJECT_ADMIN, null)).thenReturn(List.of(profile("Trần Thu Hà", Role.SUBJECT_ADMIN)));

        var contacts = service.contacts(UUID.randomUUID(), "SYSTEM_ADMIN", null);

        assertEquals(List.of("Nguyễn Văn An", "Trần Thu Hà"),
                contacts.stream().map(UserContactQueryService.ChatContact::displayName).toList());
    }

    @Test
    void regularUserReceivesFacultySubjectAdminsAndSystemAdmins() {
        when(users.findActiveAudience(Role.SUBJECT_ADMIN, "CNTT"))
                .thenReturn(List.of(profile("Quản trị chuyên môn", Role.SUBJECT_ADMIN)));
        when(users.findActiveAudience(Role.SYSTEM_ADMIN, null))
                .thenReturn(List.of(profile("Quản trị hệ thống", Role.SYSTEM_ADMIN)));

        var contacts = service.contacts(UUID.randomUUID(), "USER", "CNTT");

        assertEquals(List.of(Role.SUBJECT_ADMIN.name(), Role.SYSTEM_ADMIN.name()),
                contacts.stream().map(UserContactQueryService.ChatContact::role).toList());
    }

    @Test
    void subjectAdminCanContactFacultyUsersAndSystemAdmins() {
        when(users.findActiveAudience(Role.USER, "CNTT"))
                .thenReturn(List.of(profile("Giảng viên", Role.USER)));
        when(users.findActiveAudience(Role.SYSTEM_ADMIN, null))
                .thenReturn(List.of(profile("Quản trị hệ thống", Role.SYSTEM_ADMIN)));

        var contacts = service.contacts(UUID.randomUUID(), "SUBJECT_ADMIN", "CNTT");

        assertEquals(List.of(Role.USER.name(), Role.SYSTEM_ADMIN.name()),
                contacts.stream().map(UserContactQueryService.ChatContact::role).toList());
    }

    private UserProfile profile(String name, Role role) {
        Instant now = Instant.parse("2026-09-27T00:00:00Z");
        return new UserProfile(UUID.randomUUID(), "GV001", name, null, null,
                "gv001@hau.edu.vn", null, null, null, null, null, "CNTT",
                role, UserStatus.ACTIVE, now, now, 0);
    }
}
