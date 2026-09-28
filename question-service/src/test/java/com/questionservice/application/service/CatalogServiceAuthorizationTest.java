package com.questionservice.application.service;

import com.questionservice.application.model.LecturerProfile;
import com.questionservice.application.port.out.CatalogRepository;
import com.questionservice.application.port.out.SubjectAssignmentRepository;
import com.questionservice.application.port.out.UserDirectoryPort;
import com.questionservice.domain.exception.ForbiddenException;
import com.questionservice.domain.model.Actor;
import com.questionservice.domain.model.Role;
import com.questionservice.domain.model.Subject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CatalogServiceAuthorizationTest {
    private final CatalogRepository catalog = mock(CatalogRepository.class);
    private final SubjectAssignmentRepository assignments = mock(SubjectAssignmentRepository.class);
    private final UserDirectoryPort users = mock(UserDirectoryPort.class);
    private final CatalogService service = new CatalogService(catalog,
            Clock.fixed(Instant.parse("2026-09-28T00:00:00Z"), ZoneOffset.UTC), assignments, users);
    private final UUID subjectId = UUID.randomUUID();
    private final UUID lecturerId = UUID.randomUUID();
    private Subject subject;

    @BeforeEach
    void setUp() {
        subject = new Subject(subjectId, "CNTT", Set.of("CNTT", "KT"), "TRIET", "Triết học",
                Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-01-01T00:00:00Z"));
        when(catalog.findSubject(subjectId)).thenReturn(Optional.of(subject));
        when(users.findLecturer(lecturerId)).thenReturn(Optional.of(
                new LecturerProfile(lecturerId, "GV001", "Giảng viên", "KT", "USER", "ACTIVE", "NONE", "TS", null)));
    }

    @Test
    void systemAdminCanAssignLecturerFromParticipatingFaculty() {
        Actor actor = new Actor(UUID.randomUUID(), Role.SYSTEM_ADMIN, null);
        service.assignLecturer(subjectId, lecturerId, actor);
        verify(assignments).assign(subjectId, lecturerId, actor.userId(), Instant.parse("2026-09-28T00:00:00Z"));
    }

    @Test
    void managingSubjectAdminCanAssignLecturerFromParticipatingFaculty() {
        Actor actor = new Actor(UUID.randomUUID(), Role.SUBJECT_ADMIN, "CNTT");
        service.assignLecturer(subjectId, lecturerId, actor);
        verify(assignments).assign(subjectId, lecturerId, actor.userId(), Instant.parse("2026-09-28T00:00:00Z"));
    }

    @Test
    void participatingSubjectAdminCannotManageCanonicalAssignments() {
        Actor actor = new Actor(UUID.randomUUID(), Role.SUBJECT_ADMIN, "KT");
        ForbiddenException error = assertThrows(ForbiddenException.class,
                () -> service.assignLecturer(subjectId, lecturerId, actor));
        assertEquals("SUBJECT_ACCESS_DENIED", error.code());
        verifyNoInteractions(assignments);
    }

    @Test
    void regularUserCannotAssignLecturer() {
        Actor actor = new Actor(UUID.randomUUID(), Role.USER, "CNTT");
        ForbiddenException error = assertThrows(ForbiddenException.class,
                () -> service.assignLecturer(subjectId, lecturerId, actor));
        assertEquals("SUBJECT_ACCESS_DENIED", error.code());
    }
}
