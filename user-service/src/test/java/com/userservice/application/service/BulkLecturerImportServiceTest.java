package com.userservice.application.service;

import com.userservice.application.dto.ActorContext;
import com.userservice.application.port.out.LecturerWorkbookReader;
import com.userservice.application.port.out.UserEventPublisher;
import com.userservice.domain.exception.ForbiddenOperationException;
import com.userservice.domain.model.Faculty;
import com.userservice.domain.model.Role;
import com.userservice.domain.model.UserStatus;
import com.userservice.domain.repository.FacultyRepository;
import com.userservice.domain.repository.UserProfileRepository;
import java.io.ByteArrayInputStream;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BulkLecturerImportServiceTest {
    private static final Instant NOW = Instant.parse("2026-09-28T00:00:00Z");

    @Test
    void previewReportsValidInvalidAndDuplicateRowsWithoutWriting() {
        var workbook = mock(LecturerWorkbookReader.class);
        var users = mock(UserProfileRepository.class);
        var faculties = mock(FacultyRepository.class);
        var events = mock(UserEventPublisher.class);
        when(workbook.read(any(), any(), anyLong(), any())).thenReturn(List.of(
                new LecturerWorkbookReader.Row(2, "GV01", "Nguyễn A", null, "PGS", "TS", "a@hau.edu.vn", "CNTT"),
                new LecturerWorkbookReader.Row(3, "GV01", "Nguyễn B", null, "NONE", "NONE", "not-an-email", "CNTT"),
                new LecturerWorkbookReader.Row(4, "GV03", "Nguyễn C", null, "NONE", "NONE", "c@hau.edu.vn", "UNKNOWN")));
        when(faculties.findByCode("CNTT")).thenReturn(Optional.of(faculty("CNTT")));

        var result = service(workbook, users, faculties, events).preview(admin(), "lecturers.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 100,
                new ByteArrayInputStream(new byte[0]));

        assertThat(result.total()).isEqualTo(3);
        assertThat(result.validCount()).isEqualTo(1);
        assertThat(result.duplicateCount()).isEqualTo(1);
        assertThat(result.errorCount()).isEqualTo(1);
        verify(users, never()).save(any());
        verifyNoInteractions(events);
    }

    @Test
    void confirmIsAtomicAtValidationBoundaryAndCreatesPendingAccounts() {
        var workbook = mock(LecturerWorkbookReader.class);
        var users = mock(UserProfileRepository.class);
        var faculties = mock(FacultyRepository.class);
        var events = mock(UserEventPublisher.class);
        when(workbook.read(any(), any(), anyLong(), any())).thenReturn(List.of(
                new LecturerWorkbookReader.Row(2, "GV01", "Nguyễn A", "0901", "PGS", "TS", "a@hau.edu.vn", "CNTT")));
        when(faculties.findByCode("CNTT")).thenReturn(Optional.of(faculty("CNTT")));
        when(users.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        UUID correlationId = UUID.randomUUID();

        var result = service(workbook, users, faculties, events).confirm(admin(), "lecturers.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 100,
                new ByteArrayInputStream(new byte[0]), correlationId);

        assertThat(result.importedCount()).isEqualTo(1);
        var saved = org.mockito.ArgumentCaptor.forClass(com.userservice.domain.model.UserProfile.class);
        verify(users).save(saved.capture());
        assertThat(saved.getValue().getStatus()).isEqualTo(UserStatus.PENDING_APPROVAL);
        assertThat(saved.getValue().getRole()).isEqualTo(Role.USER);
        verify(events).accountImportRequested(saved.getValue(), correlationId);
    }

    @Test
    void nonAdminCannotPreview() {
        var service = service(mock(LecturerWorkbookReader.class), mock(UserProfileRepository.class),
                mock(FacultyRepository.class), mock(UserEventPublisher.class));
        assertThatThrownBy(() -> service.preview(new ActorContext(UUID.randomUUID(), Role.USER, "CNTT"),
                "lecturers.xlsx", "application/octet-stream", 10, new ByteArrayInputStream(new byte[0])))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    private BulkLecturerImportService service(LecturerWorkbookReader workbook, UserProfileRepository users,
                                               FacultyRepository faculties, UserEventPublisher events) {
        return new BulkLecturerImportService(workbook, users, faculties, events, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private ActorContext admin() {
        return new ActorContext(UUID.randomUUID(), Role.SYSTEM_ADMIN, null);
    }

    private Faculty faculty(String code) {
        return new Faculty(UUID.randomUUID(), code, "Khoa CNTT", null, true, NOW, NOW);
    }
}
